package org.jboss.test.audit.report;

import static javax.lang.model.SourceVersion.RELEASE_6;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import org.jboss.test.audit.annotations.SpecAssertion;
import org.jboss.test.audit.annotations.SpecAssertions;
import org.jboss.test.audit.annotations.SpecVersion;
import org.jboss.test.audit.config.RuntimeProperties;
import org.jboss.test.audit.generate.SectionsClassGenerator;

/**
 * Annotation processor for generating TCK coverage report
 *
 * @author Shane Bryzak
 * @author Hardy Ferentschik
 */
@SupportedAnnotationTypes({
        "org.jboss.test.audit.annotations.SpecAssertion",
        "org.jboss.test.audit.annotations.SpecAssertions"
})
@SupportedSourceVersion(RELEASE_6)
public class CoverageProcessor extends AbstractProcessor {

	private static final String GENERATED_SOURCE_PACKAGE_OPTION_FLAG = "generatedSourcesPackage";
	private static final String GENERATED_SOURCE_OUTDIR_OPTION_FLAG = "generatedSourcesOutputDir";
	private static final String OUTDIR_OPTION_FLAG = "outputDir";
    private static final String AUDITFILE_OPTION_KEY = "auditXml";

    private static final String DEFAULT_AUDIT_FILE_NAME = "test-audit.xml";

    private RuntimeProperties properties = new RuntimeProperties();

    private final Map<String,List<SpecReference>> references = new HashMap<String,List<SpecReference>>();

    private Map<String,AuditParser> auditParsers;

    private File baseDir;
    private File generatedSourcesOutputDir;

    public CoverageProcessor() {
    }

    public void init(ProcessingEnvironment env) {
        super.init(env);

        createOutputDirs();

        File[] auditFiles = getAuditFiles();
        auditParsers = new HashMap<String,AuditParser>();

        for (File f : auditFiles)
        {
           InputStream in = getAuditFileInputStream(f);

           if (in == null) {
               return;
           }

           AuditParser auditParser = null;

           try {
               auditParser = new AuditParser(in, properties);
               auditParser.parse();
               auditParsers.put(auditParser.getSpecId(), auditParser);
           }
           catch (Exception e) {
              e.printStackTrace();
               throw new RuntimeException("Unable to parse audit file.", e);
           }

           if(auditParser.isGenerateSectionClass()) {
        	   try {
        		   SectionsClassGenerator sectionsClassGenerator = new SectionsClassGenerator();
        		   if(generatedSourcesOutputDir != null) {
        			   sectionsClassGenerator.generateToFile(generatedSourcesOutputDir, getAuditFileInputStream(f), env.getOptions().get(GENERATED_SOURCE_PACKAGE_OPTION_FLAG));
        		   } else {
        			   sectionsClassGenerator.generateToJavaFileObject(env, getAuditFileInputStream(f), env.getOptions().get(GENERATED_SOURCE_PACKAGE_OPTION_FLAG));
        		   }
        	   } catch (Exception e) {
        		   throw new RuntimeException("Unable to generate class with section constants.", e);
			}
           }
        }
    }

    private InputStream getAuditFileInputStream(File file) {
        InputStream in;
        try {
            in = new FileInputStream(file);
        }
        catch (IOException ex) {
           System.err.println("Unable to open audit file - " + file.getAbsolutePath());
           System.err.println("No report generated");
           return null;
        }
        return in;
    }

    private File[] getAuditFiles()
    {
       String auditFileNames = processingEnv.getOptions().get(AUDITFILE_OPTION_KEY);

       if (auditFileNames == null || auditFileNames.length() == 0)
       {
          auditFileNames = getCurrentWorkingDirectory() + DEFAULT_AUDIT_FILE_NAME;
           System.out.println(
                   "No audit file specified. Trying default: " + auditFileNames
           );
       }
       else
       {
          System.out.println(
             "Reading spec assertions from audit file/s: " + auditFileNames);
       }

       String[] parts = auditFileNames.split(",");

       File[] files = new File[parts.length];

       for (int i = 0; i < parts.length; i++)
       {
          files[i] = new File(parts[i]);
       }

       return files;
    }

    private File getImagesDir() {
       return new File(getAuditFiles()[0].getParentFile(), "/images");
    }

    private void createOutputDirs() {
        String baseDirName = processingEnv.getOptions().get(OUTDIR_OPTION_FLAG);

        // I would like to get the baseDir as property, but it seems that the maven compiler plugin still has issues - http://jira.codehaus.org/browse/MCOMPILER-75
        if (baseDirName == null) {
            baseDirName = getCurrentWorkingDirectory() + "target";
           System.out.println(
                    "No output directory specified, using " + baseDirName + " instead."
            );
        }
        else
        {
           System.out.println(
                 "Outputting to " + baseDirName
         );
        }

        baseDir = new File(baseDirName);
        baseDir.mkdirs();

        // Generated source dir
        String sectionsOutputDirName = processingEnv.getOptions().get(GENERATED_SOURCE_OUTDIR_OPTION_FLAG);
        if(sectionsOutputDirName != null) {
        	generatedSourcesOutputDir = new File(sectionsOutputDirName);
        	generatedSourcesOutputDir.mkdirs();
        }
    }

    private String getCurrentWorkingDirectory() {
        return System.getProperty("user.dir") + System.getProperty("file.separator");
    }

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {

        if (auditParsers.isEmpty()) {
            return false;
        }

        for (TypeElement type : annotations) {
            processAnnotatedMethods(roundEnvironment, type);
        }

		if (roundEnvironment.processingOver()) {
			for (AuditParser auditParser : auditParsers.values()) {
				try {
					new CoverageReport(references.get(auditParser.getSpecId()),
							auditParser, getImagesDir(), properties)
							.generateToOutputDir(baseDir);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
        }
        return false;
    }

    private void processAnnotatedMethods(RoundEnvironment env, TypeElement annotation) {
        Set<Element> elements = (Set<Element>) env.getElementsAnnotatedWith(annotation);
        for (Element element : elements) {
            processMethod(element);
        }
    }

    private void processMethod(Element element) {
        ExecutableElement methodElement = (ExecutableElement) element;
        String annotationType = null;
        for (AnnotationMirror annotationMirror : processingEnv.getElementUtils().getAllAnnotationMirrors(methodElement)) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> annotationParameters =
                    processingEnv.getElementUtils().getElementValuesWithDefaults(annotationMirror);
            annotationType = annotationMirror.getAnnotationType().toString();
            if (annotationType.equals(SpecAssertions.class.getName())) {
                List<AnnotationMirror> mirrors = (List<AnnotationMirror>) annotationMirror.getElementValues().values().iterator().next().getValue();
                for (AnnotationMirror mirror : mirrors) {
                    createSpecReference(methodElement, processingEnv.getElementUtils().getElementValuesWithDefaults(mirror));
                }
            } else if (annotationType.equals(SpecAssertion.class.getName())) {
                createSpecReference(methodElement, annotationParameters);
            }
        }
    }

    private void createSpecReference(ExecutableElement methodElement,
          Map<? extends ExecutableElement, ? extends AnnotationValue> annotationParameters) {

        SpecReference ref = new SpecReference();

        PackageElement packageElement = getEnclosingPackageElement(methodElement);
        ref.setPackageName(packageElement.getQualifiedName().toString());
        ref.setClassName(methodElement.getEnclosingElement().getSimpleName().toString());
        ref.setMethodName(methodElement.getSimpleName().toString());

        if (methodElement.getEnclosingElement().getAnnotation(SpecVersion.class) != null)
        {
           ref.setSpecId(methodElement.getEnclosingElement().getAnnotation(SpecVersion.class).spec());
           ref.setSpecVersion(methodElement.getEnclosingElement().getAnnotation(SpecVersion.class).version());
        }

        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationParameters.entrySet()) {
            final String elementKey = entry.getKey().toString();

            if (elementKey.equals("section()")) {
                ref.setSection((String) entry.getValue().getValue());
            } else if (elementKey.equals("id()")) {
                ref.setAssertion((String) entry.getValue().getValue());
            }
        }
        for (AnnotationMirror annotationMirror : processingEnv.getElementUtils().getAllAnnotationMirrors(methodElement))
        {
           if (annotationMirror.getAnnotationType().toString().equals("org.testng.annotations.Test"))
           {
              Map<? extends ExecutableElement, ? extends AnnotationValue> testAnnotationParameters =
                 processingEnv.getElementUtils().getElementValuesWithDefaults(annotationMirror);
              for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : testAnnotationParameters.entrySet()) {
                 final String elementKey = entry.getKey().toString();

                 if (elementKey.equals("groups()")) {
                     for (AnnotationValue annotationValue : (List<? extends AnnotationValue>) entry.getValue().getValue())
                     {
                        ref.getGroups().add((String) annotationValue.getValue());
                     }
                 }
             }
           }
        }

        List<SpecReference> refs = references.get(ref.getSpecId());
        if (refs == null)
        {
           refs = new ArrayList<SpecReference>();
           references.put(ref.getSpecId(), refs);
        }

        refs.add(ref);
    }

    private PackageElement getEnclosingPackageElement(ExecutableElement methodElement) {
       Element classElement = methodElement.getEnclosingElement();

       Element enclosingElement = classElement.getEnclosingElement();

       while (!(enclosingElement instanceof PackageElement) && enclosingElement != null) {
          enclosingElement = enclosingElement.getEnclosingElement();
       }

       return (PackageElement) enclosingElement;
    }
}

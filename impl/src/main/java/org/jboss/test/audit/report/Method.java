/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.test.audit.report;

import java.util.Comparator;
import java.util.List;

/**
 * @author pmuir
 */
public class Method {

    private static class MethodComparator implements Comparator<Method> {

        @Override
        public int compare(Method arg0, Method arg1) {
            return arg0.getFqn().compareToIgnoreCase(arg1.getFqn());
        }

    }

    public static final Comparator<Method> COMPARATOR = new MethodComparator();

    private final String packageName;
    private final String className;
    private final String methodName;
    private final List<String> groups;

    public Method(String packageName, String className, String methodName, List<String> groups) {
        this.packageName = packageName;
        this.className = className;
        this.methodName = methodName;
        this.groups = groups;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getGroups() {
        return groups;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Method) {
            Method that = (Method) obj;
            return this.getPackageName().equals(that.getPackageName()) && this.getClassName().equals(that.getClassName()) && this.getMethodName()
                    .equals(that.getMethodName());
        } else {
            return false;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return getPackageName().hashCode() + getClassName().hashCode() + getMethodName().hashCode();
    }

    public String getFqn() {
        return packageName + "." + className + "." + methodName;
    }

}

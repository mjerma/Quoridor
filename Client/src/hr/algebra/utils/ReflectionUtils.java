/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

public class ReflectionUtils {

    private static final String H2_OPEN = "<h2>";
    private static final String H2_CLOSE = "</h2>";
    private static final String H3_OPEN = "<h3>";
    private static final String H3_CLOSE = "</h3>";
    private static final String P_OPEN = "<p>";
    private static final String P_CLOSE = "</p>";
    private static final String BR_TAG = "</br>";
    private static final String HR_TAG = "<hr>";


    public static void readClassInfo(Class<?> clazz, StringBuilder classInfo) {
        appendPackage(clazz, classInfo);
        appendModifiers(clazz, classInfo);
        appendParent(clazz, classInfo, true);
        appendInterfaces(clazz, classInfo);
    }

    private static void appendPackage(Class<?> clazz, StringBuilder classInfo) {
        classInfo
                .append(H2_OPEN)
                .append(clazz.getPackage())
                .append(H2_CLOSE)
                .append(BR_TAG)
                .append("\n\n");
    }

    private static void appendModifiers(Class<?> clazz, StringBuilder classInfo) {
        classInfo
                .append(H3_OPEN)
                .append(Modifier.toString(clazz.getModifiers()))
                .append(" ")
                .append(clazz.getSimpleName())
                .append(H3_CLOSE)
                .append(BR_TAG)
                .append("\n");
    }

    private static void appendParent(Class<?> clazz, StringBuilder classInfo, boolean first) {
        Class<?> parent = clazz.getSuperclass();
        if (parent == null) {
            return;
        }
        if (first) {
            classInfo
                    .append(H3_OPEN)
                    .append("extends");
        }
        classInfo
                .append(" ")
                .append(parent.getName());
        appendParent(parent, classInfo, false);

        if (first) {
            classInfo
                    .append(H3_CLOSE)
                    .append(BR_TAG);
        }
    }

    private static void appendInterfaces(Class<?> clazz, StringBuilder classInfo) {
        if (clazz.getInterfaces().length > 0) {
            classInfo.append("\n");
            classInfo.append(H3_OPEN);
            classInfo.append("implements");
        }
        for (Class<?> in : clazz.getInterfaces()) {
            classInfo
                    .append(" ")
                    .append(in.getName())
                    .append(H3_CLOSE)
                    .append(BR_TAG);
        }
    }

    public static void readClassAndMembersInfo(Class<?> clazz, StringBuilder classAndMembersInfo) {
        readClassInfo(clazz, classAndMembersInfo);
        appendFields(clazz, classAndMembersInfo);
        appendMethods(clazz, classAndMembersInfo);
        appendConstructors(clazz, classAndMembersInfo);
    }

    private static void appendFields(Class<?> clazz, StringBuilder classAndMembersInfo) {
        //Field[] fields = clazz.getFields(); // returns public and inherited
        Field[] fields = clazz.getDeclaredFields(); // returns public, protected, default (package) access, and private fields, but excludes inherited fields
        classAndMembersInfo.append("\n\n");
        for (Field field : fields) {
            classAndMembersInfo
                    .append(P_OPEN)
                    .append(field)
                    .append(P_CLOSE)
                    .append(BR_TAG)
                    .append("\n");
        }
    }

    private static void appendMethods(Class<?> clazz, StringBuilder classAndMembersInfo) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            classAndMembersInfo.append("\n");
            appendMethodAnnotations(method, classAndMembersInfo);
            classAndMembersInfo
                    .append(P_OPEN)
                    .append(Modifier.toString(method.getModifiers()))
                    .append(" ")
                    .append(method.getReturnType())
                    .append(" ")
                    .append(method.getName());
            appendParameters(method, classAndMembersInfo);
            appendExceptions(method, classAndMembersInfo);
        }
        classAndMembersInfo.append("\n");
    }

    private static void appendMethodAnnotations(Executable executable, StringBuilder classAndMembersInfo) {
        for (Annotation annotation : executable.getAnnotations()) {
            classAndMembersInfo
                    .append(P_OPEN)
                    .append(annotation)
                    .append("\n");
        }
    }

    private static void appendParameters(Executable executable, StringBuilder classAndMembersInfo) {
        classAndMembersInfo.append("(");
        for (Parameter parameter : executable.getParameters()) {
            classAndMembersInfo
                    .append(parameter)
                    .append(", ");
        }
        if (classAndMembersInfo.toString().endsWith(", ")) {
            classAndMembersInfo.delete(classAndMembersInfo.length() - 2, classAndMembersInfo.length());
        }
        classAndMembersInfo
                .append(")")
                .append(P_CLOSE)
                .append(BR_TAG)
                .append("\n");
    }

    private static void appendExceptions(Executable executable, StringBuilder classAndMembersInfo) {
        Class<?>[] exceptionTypes = executable.getExceptionTypes();
        if (exceptionTypes.length > 0) {
            classAndMembersInfo.append(P_OPEN);
            classAndMembersInfo.append("throws ");
            for (Class<?> exceptionType : exceptionTypes) {
                classAndMembersInfo
                        .append(exceptionType)
                        .append(", ");
            }
            if (classAndMembersInfo.toString().endsWith(", ")) {
                classAndMembersInfo.delete(classAndMembersInfo.length() - 2, classAndMembersInfo.length());
            }
            classAndMembersInfo
                    .append(P_CLOSE)
                    .append(BR_TAG);

        }
    }

    private static void appendConstructors(Class<?> clazz, StringBuilder classAndMembersInfo) {
        Constructor[] constructors = clazz.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            classAndMembersInfo.append("\n");
            appendMethodAnnotations(constructor, classAndMembersInfo);
            classAndMembersInfo
                    .append(P_OPEN)
                    .append(Modifier.toString(constructor.getModifiers()))
                    .append(" ")
                    .append(constructor.getName());
            appendParameters(constructor, classAndMembersInfo);
            appendExceptions(constructor, classAndMembersInfo);
        }

        classAndMembersInfo
                .append(BR_TAG)
                .append(HR_TAG)
                .append(BR_TAG)
                .append("\n");
    }
}

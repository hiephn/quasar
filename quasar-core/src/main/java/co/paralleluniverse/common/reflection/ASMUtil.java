/*
 * Quasar: lightweight threads and actors for the JVM.
 * Copyright (C) 2013, Parallel Universe Software Co. All rights reserved.
 * 
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *  
 *   or (per the licensee's choosing)
 *  
 * under the terms of the GNU Lesser General Public License version 3.0
 * as published by the Free Software Foundation.
 */
package co.paralleluniverse.common.reflection;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

/**
 *
 * @author pron
 */
public final class ASMUtil {
    private static final String CLASSFILE_SUFFIX = ".class";
    
    public static boolean isClassFileName(String filename) {
        return filename.endsWith(CLASSFILE_SUFFIX);
    }

    public static String toDottedName(String className) {
        if (className.endsWith(CLASSFILE_SUFFIX))
            className = className.substring(0, className.length() - CLASSFILE_SUFFIX.length());
        return className.replace('/', '.');
    }

    public static String toSlashName(String className) {
        return className.replace('.', '/');
    }

    public static String toClassFileName(String className) {
        return toSlashName(className) + CLASSFILE_SUFFIX;
    }

    public static String toSlashName(Class<?> clazz) {
        return toSlashName(clazz.getName());
    }

    public static String toClassFileName(Class<?> clazz) {
        return toClassFileName(clazz.getName());
    }

    public static byte[] getClass(String className, ClassLoader cl) throws IOException {
        try (InputStream is = cl.getResourceAsStream(toClassFileName(className))) {
            return ByteStreams.toByteArray(is);
        }
    }

    public static ClassNode getClassNode(String className, boolean skipCode, ClassLoader cl) throws IOException {
        if (className == null)
            return null;
        try (InputStream is = cl.getResourceAsStream(toClassFileName(className))) {
            ClassReader cr = new ClassReader(is);
            ClassNode cn = new ClassNode();
            cr.accept(cn, ClassReader.SKIP_DEBUG | (skipCode ? 0 : ClassReader.SKIP_CODE));
            return cn;
        }
    }

    public static ClassNode getClassNode(File classFile, boolean skipCode) throws IOException {
        if (classFile == null)
            return null;
        if (!classFile.exists())
            return null;
        try (InputStream is = new FileInputStream(classFile)) {
            ClassReader cr = new ClassReader(is);
            ClassNode cn = new ClassNode();
            cr.accept(cn, ClassReader.SKIP_DEBUG | (skipCode ? 0 : ClassReader.SKIP_CODE));
            return cn;
        }
    }

    public static boolean hasAnnotation(String annDesc, List<AnnotationNode> anns) {
        if (anns == null)
            return false;
        for (AnnotationNode ann : anns) {
            if (ann.desc.equals(annDesc))
                return true;
        }
        return false;
    }

    public static boolean hasAnnotation(Class ann, List<AnnotationNode> anns) {
        return hasAnnotation(Type.getDescriptor(ann), anns);
    }

    public static boolean hasAnnotation(String annDesc, ClassNode c) {
        return hasAnnotation(annDesc, c.visibleAnnotations);
    }

    public static boolean hasAnnotation(Class ann, ClassNode c) {
        return hasAnnotation(ann, c.visibleAnnotations);
    }

    public static boolean hasAnnotation(String annDesc, MethodNode m) {
        return hasAnnotation(annDesc, m.visibleAnnotations);
    }

    public static boolean hasAnnotation(Class ann, MethodNode m) {
        return hasAnnotation(ann, m.visibleAnnotations);
    }

    public static boolean hasAnnotation(String annDesc, FieldNode f) {
        return hasAnnotation(annDesc, f.visibleAnnotations);
    }

    public static boolean hasAnnotation(Class ann, FieldNode f) {
        return hasAnnotation(ann, f.visibleAnnotations);
    }

    public static boolean hasMethod(MethodNode method, List<MethodNode> ms) {
        if (ms == null)
            return false;
        for (MethodNode m : ms) {
            if (equals(method, m))
                return true;
        }
        return false;
    }

    public static boolean hasMethod(MethodNode method, ClassNode c) {
        return hasMethod(method, c.methods);
    }

    public static boolean equals(MethodNode m1, MethodNode m2) {
        return Objects.equals(m1.name, m2.name) && Objects.equals(m1.signature, m2.signature);
    }

    public static boolean equals(ClassNode c1, ClassNode c2) {
        return Objects.equals(c1.name, c2.name);
    }

    public static boolean isAssignableFrom(Class<?> supertype, String className, ClassLoader cl) {
        return isAssignableFrom(supertype.getName(), className, cl);
    }

    public static boolean isAssignableFrom(String supertypeName, String className, ClassLoader cl) {
        try {
            if (className == null)
                return false;
            if (supertypeName.equals(className))
                return true;
            ClassNode cn = getClassNode(className, true, cl);

            if (supertypeName.equals(cn.superName))
                return true;
            if (isAssignableFrom(supertypeName, cn.superName, cl))
                return true;

            if (cn.interfaces != null) {
                for (String iface : (List<String>) cn.interfaces) {
                    if (supertypeName.equals(iface))
                        return true;
                    if (isAssignableFrom(supertypeName, iface, cl))
                        return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    private ASMUtil() {
    }
}

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.builder.testing;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteStreams;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Given a "standard" android.jar, creates a "mockable" version, where all classes and methods
 * are not final. Optionally makes all methods return "default" values, instead of throwing the
 * infamous "Stub!" exceptions.
 */
public class MockableJarGenerator {
    private static final int EMPTY_FLAGS = 0;
    private static final String CONSTRUCTOR = "<init>";
    private static final ImmutableSet<Type> INTEGER_LIKE_TYPES = ImmutableSet.of(
            Type.INT_TYPE, Type.BYTE_TYPE, Type.BOOLEAN_TYPE, Type.CHAR_TYPE, Type.SHORT_TYPE);

    private final boolean returnDefaultValues;

    public MockableJarGenerator(boolean returnDefaultValues) {
        this.returnDefaultValues = returnDefaultValues;
    }

    public void createMockableJar(File input, File output) throws IOException {
        Preconditions.checkState(
                output.createNewFile(),
                "Output file [%s] already exists.",
                output.getAbsolutePath());

        JarFile androidJar = null;
        JarOutputStream outputStream = null;
        try {
            androidJar = new JarFile(input);
            outputStream = new JarOutputStream(new FileOutputStream(output));

            for (JarEntry entry : Collections.list(androidJar.entries())) {
                InputStream inputStream = androidJar.getInputStream(entry);

                if (entry.getName().endsWith(".class")) {
                    rewriteClass(entry, inputStream, outputStream);
                } else {
                    outputStream.putNextEntry(entry);
                    ByteStreams.copy(inputStream, outputStream);
                }

                inputStream.close();
            }
        } finally {
            if (androidJar != null) {
                androidJar.close();
            }
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    /**
     * Writes a modified *.class file to the output JAR file.
     */
    private void rewriteClass(
            JarEntry entry,
            InputStream inputStream,
            JarOutputStream outputStream) throws IOException {
        ClassReader classReader = new ClassReader(inputStream);
        ClassNode classNode = new ClassNode(Opcodes.ASM4);

        classReader.accept(classNode, EMPTY_FLAGS);

        modifyClass(classNode);

        ClassWriter classWriter = new ClassWriter(EMPTY_FLAGS);
        classNode.accept(classWriter);

        outputStream.putNextEntry(new ZipEntry(entry.getName()));
        outputStream.write(classWriter.toByteArray());
    }

    /**
     * Modifies a {@link ClassNode} to clear final flags and rewrite byte code.
     */
    @SuppressWarnings("unchecked")
    private void modifyClass(ClassNode classNode) {
        // Make the class not final.
        classNode.access &= ~Opcodes.ACC_FINAL;

        List<MethodNode> methodNodes = classNode.methods;
        for (MethodNode methodNode : methodNodes) {
            methodNode.access &= ~Opcodes.ACC_FINAL;
            fixMethodBody(methodNode);
        }
    }

    /**
     * Rewrites the method bytecode to remove the "Stub!" exception.
     */
    private void fixMethodBody(MethodNode methodNode) {
        if ((methodNode.access & Opcodes.ACC_NATIVE) != 0
                || (methodNode.access & Opcodes.ACC_ABSTRACT) != 0) {
            // Abstract and native method don't have bodies to rewrite.
            return;
        }

        Type returnType = Type.getReturnType(methodNode.desc);
        InsnList instructions = methodNode.instructions;

        if (methodNode.name.equals(CONSTRUCTOR)) {
            // Keep the call to parent constructor, delete the exception after that.

            boolean deadCode = false;
            for (AbstractInsnNode instruction : instructions.toArray()) {
                if (!deadCode) {
                    if (instruction.getOpcode() == Opcodes.INVOKESPECIAL) {
                        if (returnDefaultValues) {
                            instructions.insert(instruction, new InsnNode(Opcodes.RETURN));
                        } else {
                            instructions.insert(instruction, throwExceptionsList(methodNode));
                        }
                        // Start removing all following instructions.
                        deadCode = true;
                    }
                } else {
                    instructions.remove(instruction);
                }
            }
        } else {
            instructions.clear();

            if (returnDefaultValues) {
                if (returnType.equals(Type.VOID_TYPE)) {
                  return;
                } else if (INTEGER_LIKE_TYPES.contains(returnType)) {
                    instructions.add(new InsnNode(Opcodes.ICONST_0));
                } else if (returnType.equals(Type.LONG_TYPE)) {
                    instructions.add(new InsnNode(Opcodes.LCONST_0));
                } else if (returnType.equals(Type.FLOAT_TYPE)) {
                    instructions.add(new InsnNode(Opcodes.FCONST_0));
                } else if (returnType.equals(Type.DOUBLE_TYPE)) {
                    instructions.add(new InsnNode(Opcodes.DCONST_0));
                } else {
                    instructions.add(new InsnNode(Opcodes.ACONST_NULL));
                }

                instructions.add(new InsnNode(returnType.getOpcode(Opcodes.IRETURN)));
            } else {
                instructions.insert(throwExceptionsList(methodNode));
            }
        }
    }

    private static InsnList throwExceptionsList(MethodNode methodNode) {
        try {
            String runtimeException = Type.getInternalName(RuntimeException.class);
            Constructor<RuntimeException> constructor =
                    RuntimeException.class.getConstructor(String.class);

            InsnList instructions = new InsnList();
            instructions.add(
                    new TypeInsnNode(Opcodes.NEW, runtimeException));
            instructions.add(new InsnNode(Opcodes.DUP));
            instructions.add(new LdcInsnNode("Method " + methodNode.name + " not mocked."));
            instructions.add(new MethodInsnNode(
                    Opcodes.INVOKESPECIAL,
                    runtimeException,
                    CONSTRUCTOR,
                    Type.getType(constructor).getDescriptor()));
            instructions.add(new InsnNode(Opcodes.ATHROW));

            return instructions;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
/*
 * Copyright (c) 2003, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package nsk.jvmti.ClassFileLoadHook;

import java.io.*;
import java.lang.reflect.*;

import nsk.share.*;
import nsk.share.jvmti.*;

/**
 * Debuggee class of JVMTI test.
 */
public class classfloadhk006 extends DebugeeClass {

    /** Load native library if required. */
    static {
        System.loadLibrary("classfloadhk006");
    }

    /** Run test from command line. */
    public static void main(String argv[]) {
        argv = nsk.share.jvmti.JVMTITest.commonInit(argv);

        // JCK-compatible exit
        System.exit(run(argv, System.out) + Consts.JCK_STATUS_BASE);
    }

    /** Run test from JCK-compatible environment. */
    public static int run(String argv[], PrintStream out) {
        return new classfloadhk006().runIt(argv, out);
    }

    /* =================================================================== */

    /* constant names */
    public static final String PACKAGE_NAME = "nsk.jvmti.ClassFileLoadHook";
    public static final String TESTED_CLASS_NAME = PACKAGE_NAME + ".classfloadhk006r";

    /* scaffold objects */
    ArgumentHandler argHandler = null;
    Log log = null;
    long timeout = 0;
    int status = Consts.TEST_PASSED;

    /* bytecode of redefined class */
    public static byte redefClassBytes[] = null;

    /* tested class to de redefined */
    public static Class<?> testedClass = null;

    /* tested method name */
    public static final String TESTED_METHOD_NAME = "testedStaticMethod";

    /* possible values returned by tested method */
    public static final long ORIG_VALUE = 20;
    public static final long NEW_VALUE = 12;

    /** Run debuggee code. */
    public int runIt(String argv[], PrintStream out) {
        argHandler = new ArgumentHandler(argv);
        log = new Log(out, argHandler);
        timeout = argHandler.getWaitTime() * 60 * 1000; // milliseconds

        String args[] = argHandler.getArguments();
        if (args.length <= 0) {
            throw new Failure("Path for tested class file to load not specified");
        }

        String location = args[0];
        String redefPath = location + File.separator + "newclass";
        log.display("Using path to redefined class: \n\t" + redefPath);

        log.display("Reading redefined bytecode of tested class: \n\t" + TESTED_CLASS_NAME);
        try {
            redefClassBytes = readBytes(redefPath, TESTED_CLASS_NAME);
        } catch (IOException e) {
            throw new Failure("IOException in reading redefined bytecode of tested class file:\n\t" + e);
        }

        try {
            log.display("Loading original tested class: " + TESTED_CLASS_NAME);
            testedClass = Class.forName(TESTED_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            throw new Failure("Tested class not found: \n\t" + e);
        }

        log.display("Sync: tested class loaded");
        status = checkStatus(status);

        log.display("Checking if the tested class was actually redefined");
        try {
            Method meth = testedClass.getMethod(TESTED_METHOD_NAME, new Class[0]);
            Object res = meth.invoke(null, new Object[0]);
            if (!(res instanceof Long)) {
                log.complain("Tested method of redefined class retured not Long value: " + res);
                log.complain("The tested class was redefined incorrectly!");
                status = Consts.TEST_FAILED;
            } else {
                long value = ((Long)res).longValue();
                log.display("Tested method of redefined class returned value: " + value);

                if (value == ORIG_VALUE) {
                    log.complain("Tested method of redefined class returned original value: " + value);
                    log.complain("The tested class was not redefined!");
                    status = Consts.TEST_FAILED;
                } else if (value != NEW_VALUE) {
                    log.complain("Tested method of redefined class returned unexpected value: " + value);
                    log.complain("The tested class was redefined incorrectly!");
                    status = Consts.TEST_FAILED;
                } else {
                    log.display("Tested method of redefined class returned expected new value: " + value);
                    log.display("The tested class was redefined correctly!");
                }
            }
        } catch (NoSuchMethodException e) {
            log.complain("No tested method found in redefined class:\n\t" + e);
            status = Consts.TEST_FAILED;
        } catch (Exception e) {
            log.complain("Exception in invoking method of redefined class:\n\t" + e);
            status = Consts.TEST_FAILED;
        }

        return status;
    }

    /** Read classfile for specified path and class name. */
    public static byte[] readBytes(String path, String classname) throws IOException {
        String filename = path + File.separator
                                + classname.replace('.', File.separatorChar) + ".class";
        FileInputStream in = new FileInputStream(filename);
        byte[] bytes = new byte[in.available()];
        in.read(bytes);
        in.close();
        return bytes;
    }
}

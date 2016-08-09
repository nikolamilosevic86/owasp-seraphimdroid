#!/bin/bash
j2objc -d src/main/objc -use-arc -cp build/libs/lucenestudy.jar `find src/main/java -name *.java`
j2objc -d src/test/objc -use-arc -cp src/test/java:build/libs/lucenestudy.jar:$J2OBJC_ROOT/lib/j2objc_junit.jar:$J2OBJC_ROOT/lib/hamcrest-core-1.3.jar `find src/test/java -name *.java`

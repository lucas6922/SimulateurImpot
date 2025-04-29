#!/bin/bash

export JAVA_HOME="/Users/lucas/Library/Java/JavaVirtualMachines/corretto-23.0.2/Contents/Home"

export MAVEN_HOME="/opt/homebrew/Cellar/maven/3.9.9/libexec"

export PATH="$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH"

mvn clean verify site

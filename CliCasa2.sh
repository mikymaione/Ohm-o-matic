/usr/lib/jvm/jdk-11.0.2/bin/java -classpath "/home/miky/Documenti/UniMi/Sistemi Distribuiti e Pervasivi/Ohm-o-matic/target/classes:/home/miky/.m2/repository/commons-cli/commons-cli/1.4/commons-cli-1.4.jar:/home/miky/.m2/repository/org/apache/httpcomponents/httpclient/4.5.8/httpclient-4.5.8.jar:/home/miky/.m2/repository/org/apache/httpcomponents/httpcore/4.4.11/httpcore-4.4.11.jar:/home/miky/.m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:/home/miky/.m2/repository/commons-codec/commons-codec/1.11/commons-codec-1.11.jar:/home/miky/.m2/repository/io/grpc/grpc-netty-shaded/1.21.0/grpc-netty-shaded-1.21.0.jar:/home/miky/.m2/repository/io/grpc/grpc-core/1.21.0/grpc-core-1.21.0.jar:/home/miky/.m2/repository/io/opencensus/opencensus-api/0.21.0/opencensus-api-0.21.0.jar:/home/miky/.m2/repository/com/google/code/gson/gson/2.7/gson-2.7.jar:/home/miky/.m2/repository/com/google/android/annotations/4.1.1.4/annotations-4.1.1.4.jar:/home/miky/.m2/repository/io/opencensus/opencensus-contrib-grpc-metrics/0.21.0/opencensus-contrib-grpc-metrics-0.21.0.jar:/home/miky/.m2/repository/io/grpc/grpc-stub/1.21.0/grpc-stub-1.21.0.jar:/home/miky/.m2/repository/io/grpc/grpc-api/1.21.0/grpc-api-1.21.0.jar:/home/miky/.m2/repository/io/grpc/grpc-context/1.21.0/grpc-context-1.21.0.jar:/home/miky/.m2/repository/com/google/errorprone/error_prone_annotations/2.3.2/error_prone_annotations-2.3.2.jar:/home/miky/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:/home/miky/.m2/repository/org/codehaus/mojo/animal-sniffer-annotations/1.17/animal-sniffer-annotations-1.17.jar:/home/miky/.m2/repository/io/grpc/grpc-protobuf/1.21.0/grpc-protobuf-1.21.0.jar:/home/miky/.m2/repository/com/google/guava/guava/26.0-android/guava-26.0-android.jar:/home/miky/.m2/repository/org/checkerframework/checker-compat-qual/2.5.2/checker-compat-qual-2.5.2.jar:/home/miky/.m2/repository/com/google/j2objc/j2objc-annotations/1.1/j2objc-annotations-1.1.jar:/home/miky/.m2/repository/com/google/api/grpc/proto-google-common-protos/1.12.0/proto-google-common-protos-1.12.0.jar:/home/miky/.m2/repository/io/grpc/grpc-protobuf-lite/1.21.0/grpc-protobuf-lite-1.21.0.jar:/home/miky/.m2/repository/com/google/protobuf/protobuf-java/3.7.1/protobuf-java-3.7.1.jar:/home/miky/.m2/repository/javax/annotation/jsr250-api/1.0/jsr250-api-1.0.jar:/home/miky/.m2/repository/org/glassfish/jersey/containers/jersey-container-grizzly2-http/2.28/jersey-container-grizzly2-http-2.28.jar:/home/miky/.m2/repository/org/glassfish/hk2/external/jakarta.inject/2.5.0/jakarta.inject-2.5.0.jar:/home/miky/.m2/repository/org/glassfish/grizzly/grizzly-http-server/2.4.4/grizzly-http-server-2.4.4.jar:/home/miky/.m2/repository/org/glassfish/grizzly/grizzly-http/2.4.4/grizzly-http-2.4.4.jar:/home/miky/.m2/repository/org/glassfish/grizzly/grizzly-framework/2.4.4/grizzly-framework-2.4.4.jar:/home/miky/.m2/repository/org/glassfish/jersey/core/jersey-common/2.28/jersey-common-2.28.jar:/home/miky/.m2/repository/jakarta/annotation/jakarta.annotation-api/1.3.4/jakarta.annotation-api-1.3.4.jar:/home/miky/.m2/repository/org/glassfish/hk2/osgi-resource-locator/1.0.1/osgi-resource-locator-1.0.1.jar:/home/miky/.m2/repository/org/glassfish/jersey/core/jersey-server/2.28/jersey-server-2.28.jar:/home/miky/.m2/repository/org/glassfish/jersey/core/jersey-client/2.28/jersey-client-2.28.jar:/home/miky/.m2/repository/org/glassfish/jersey/media/jersey-media-jaxb/2.28/jersey-media-jaxb-2.28.jar:/home/miky/.m2/repository/javax/validation/validation-api/2.0.1.Final/validation-api-2.0.1.Final.jar:/home/miky/.m2/repository/jakarta/ws/rs/jakarta.ws.rs-api/2.1.5/jakarta.ws.rs-api-2.1.5.jar:/home/miky/.m2/repository/org/glassfish/jersey/inject/jersey-hk2/2.28/jersey-hk2-2.28.jar:/home/miky/.m2/repository/org/glassfish/hk2/hk2-locator/2.5.0/hk2-locator-2.5.0.jar:/home/miky/.m2/repository/org/glassfish/hk2/external/aopalliance-repackaged/2.5.0/aopalliance-repackaged-2.5.0.jar:/home/miky/.m2/repository/org/glassfish/hk2/hk2-api/2.5.0/hk2-api-2.5.0.jar:/home/miky/.m2/repository/org/glassfish/hk2/hk2-utils/2.5.0/hk2-utils-2.5.0.jar:/home/miky/.m2/repository/org/javassist/javassist/3.22.0-CR2/javassist-3.22.0-CR2.jar:/home/miky/.m2/repository/com/pakulov/jersey/media/jersey-media-protobuf/0.1.0/jersey-media-protobuf-0.1.0.jar" OhmOMatic.Cli.CliCasa -r http://localhost:8080/OOM/OOM -i Naruto -k 127.0.0.0 -q 7777 -j 127.0.0.0 -p 6666

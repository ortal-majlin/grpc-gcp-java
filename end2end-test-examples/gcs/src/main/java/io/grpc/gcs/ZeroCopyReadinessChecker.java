package io.grpc.gcs;

import com.google.common.flogger.GoogleLogger;
import com.google.protobuf.MessageLite;
import io.grpc.KnownLength;

/**
 * Checker to test whether a zero-copy masharller is available from the versions of gRPC and
 * Protobuf.
 */
class ZeroCopyReadinessChecker {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  private static final boolean isZeroCopyReady;

  static {
    // Check whether io.grpc.Detachable exists?
    boolean detachableClassExists = false;
    try {
      // Try to load Detachable interface in the package where KnownLength is in.
      // This can be done directly by looking up io.grpc.Detachable but rather
      // done indirectly to handle the case where gRPC is being shaded in a
      // different package.
      String knownLengthClassName = KnownLength.class.getName();
      String detachableClassName =
          knownLengthClassName.substring(0, knownLengthClassName.lastIndexOf('.') + 1)
              + "Detachable";
      Class<?> detachableClass = Class.forName(detachableClassName);
      detachableClassExists = (detachableClass != null);
    } catch (ClassNotFoundException ex) {
      logger.atFine().withCause(ex).log("io.grpc.Detachable not found");
    }
    // Check whether com.google.protobuf.UnsafeByteOperations exists?
    boolean unsafeByteOperationsClassExists = false;
    try {
      // Same above
      String messageLiteClassName = MessageLite.class.getName();
      String unsafeByteOperationsClassName =
          messageLiteClassName.substring(0, messageLiteClassName.lastIndexOf('.') + 1)
              + "UnsafeByteOperations";
      Class<?> unsafeByteOperationsClass = Class.forName(unsafeByteOperationsClassName);
      unsafeByteOperationsClassExists = (unsafeByteOperationsClass != null);
    } catch (ClassNotFoundException ex) {
      logger.atFine().withCause(ex).log("com.google.protobuf.UnsafeByteOperations not found");
    }
    isZeroCopyReady = detachableClassExists && unsafeByteOperationsClassExists;
  }

  public static boolean isReady() {
    return isZeroCopyReady;
  }
}

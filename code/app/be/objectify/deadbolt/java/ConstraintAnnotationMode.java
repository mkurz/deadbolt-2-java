package be.objectify.deadbolt.java;

public enum ConstraintAnnotationMode {

    // All constraints in the action composition chain have to be successful.
    AND,

    // All constraints in the the action composition chain will be checked until one is successful (in that case the remaining constraint will be skipped).
    OR

}

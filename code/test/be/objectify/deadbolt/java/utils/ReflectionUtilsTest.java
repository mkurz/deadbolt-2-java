package be.objectify.deadbolt.java.utils;

import org.junit.Assert;
import org.junit.Test;

public class ReflectionUtilsTest
{
    @Test
    public void testHasMethod_methodPresent()
    {
        Assert.assertTrue(ReflectionUtils.hasMethod(Object.class,
                                                    "toString"));
    }

    @Test
    public void testHasMethod_methodNotPresent()
    {
        // todo - update this test if Object ever gets this method
        Assert.assertFalse(ReflectionUtils.hasMethod(Object.class,
                                                     "aMethodThatDoesNotAndProbablyWillNotEverExist"));
    }
}
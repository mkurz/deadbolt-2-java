package be.objectify.deadbolt.java.filters;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class MethodsTest {

    @Test
    public void testAny() {
        Assert.assertFalse(Methods.ANY.isPresent());
    }

    @Test
    public void testGet() {
        Assert.assertEquals("GET",
                            Methods.GET.orElse(null));
    }

    @Test
    public void testPost() {
        Assert.assertEquals("POST",
                            Methods.POST.orElse(null));
    }

    @Test
    public void testDelete() {
        Assert.assertEquals("DELETE",
                            Methods.DELETE.orElse(null));
    }

    @Test
    public void testPut() {
        Assert.assertEquals("PUT",
                            Methods.PUT.orElse(null));
    }

    @Test
    public void testPatch() {
        Assert.assertEquals("PATCH",
                            Methods.PATCH.orElse(null));
    }

    @Test
    public void testOptions() {
        Assert.assertEquals("OPTIONS",
                            Methods.OPTIONS.orElse(null));
    }

    @Test
    public void testHead() {
        Assert.assertEquals("HEAD",
                            Methods.HEAD.orElse(null));
    }
}
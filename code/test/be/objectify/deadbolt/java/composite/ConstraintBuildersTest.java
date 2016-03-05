package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.testsupport.FakeCache;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ConstraintBuildersTest extends AbstractCompositeTest
{
    private final ConstraintBuilders builders = new ConstraintBuilders(new DeadboltAnalyzer(),
                                                                       new DefaultPatternCache(new FakeCache()));
    @Test
    public void testAllOf() throws Exception
    {
        final String[] array = builders.allOf("foo",
                                              "bar");
        Assert.assertNotNull(array);
        Assert.assertEquals(2,
                            array.length);
        Assert.assertEquals("foo",
                            array[0]);
        Assert.assertEquals("bar",
                            array[1]);
    }

    @Test
    public void testAnyOf() throws Exception
    {
        final List<String[]> list = builders.anyOf(new String[]{"foo"},
                                                   new String[]{"bar"});
        Assert.assertNotNull(list);
        Assert.assertEquals(2,
                            list.size());
        Assert.assertArrayEquals(new String[]{"foo"},
                                 list.get(0));
        Assert.assertArrayEquals(new String[]{"bar"},
                                 list.get(1));
    }
}

public class <warning descr="Java Doc!">JavaDocTestData</warning> {

    // These are needed to prevent compilation errors on the below methods with annotations
    /** Blah. **/ public @interface Override { }
    /** Blah. **/ public @interface Test { }
    /** Blah. **/ public @interface SmallTest { }
    /** Blah. **/ public @interface MediumTest { }
    /** Blah. **/ public @interface LargeTest { }

    int blah;

    // Constructors should not have warnings
    public JavaDocTestData() {
        aMethodWithoutJavadoc();
        hasOverrideAnnotation(1);
        getBlah();
        setBlah(1);
        goGetEm();
    }

    // General methods without Javadoc should have warnings
    public void <warning descr="Java Doc!">aMethodWithoutJavadoc</warning>() {
    }

    // Methods with "override" in the name but without Javadoc should still have a warning
    private boolean <warning descr="Java Doc!">hasOverrideAnnotation</warning>(int psiElement) {
        return false;
    }

    // Methods with "get" in the name but without Javadoc should still have a warning
    public void <warning descr="Java Doc!">goGetEm</warning>() {

    }

    // Getters should not have warnings
    public int getBlah() {
        return blah;
    }

    // Setters should not have warnings
    public void setBlah(int blah) {
        this.blah = blah;
    }

    // Overridden methods should not have warnings
    @Override
    public void anOverriddenMethod() {

    }

    // Methods starting with "test" should not have warnings
    public void testSomething() {

    }

    // Methods with a @Test, @SmallTest, @MediumTest, or @LargeTest annotation should not have warnings
    @Test
    public void anAnnotatedTest() {

    }

    @SmallTest
    public void anAnnotatedTest2() {

    }

    @MediumTest
    public void anAnnotatedTest3() {

    }

    @LargeTest
    public void anAnnotatedTest4() {

    }
}
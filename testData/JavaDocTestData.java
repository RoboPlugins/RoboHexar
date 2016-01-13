
public class Base {

    int blah;

    public Base() {
        overrideMe();
        hasOverrideAnnotation(1);
        getBlah();
        setBlah("blah");
        goGetEm();
    }

    public void overrideMe() {
    }

    // @Override my butt
    private boolean hasOverrideAnnotation(int psiElement) {
        return false;
    }

    public int getBlah() {
        return blah;
    }

    public void setBlah(int blah) {
        this.blah = blah;
    }

    public void goGetEm() {

    }
}
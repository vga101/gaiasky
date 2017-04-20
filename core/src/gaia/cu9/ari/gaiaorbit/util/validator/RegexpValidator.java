package gaia.cu9.ari.gaiaorbit.util.validator;

public class RegexpValidator implements IValidator {
    private String expr;

    public RegexpValidator(String expression) {
        super();
        this.expr = expression;
    }

    @Override
    public boolean validate(String value) {
        return value.matches(expr);
    }

}

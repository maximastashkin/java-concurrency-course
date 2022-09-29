package ru.rsreu.input;

public class InputEntity {
    private final InputState state;
    private String param;

    private InputEntity(Builder builder) {
        this(builder.state);
    }

    private InputEntity(InputState state) {
        this.state = state;
    }

    public InputState getState() {
        return state;
    }

    public String getParam() {
        return param;
    }

    private void setParam(String param) {
        this.param = param;
    }

    public static class Builder {
        private final String input;
        private InputState state = InputState.UNDEFINED;

        public Builder(String input) {
            this.input = input;
        }

        private static String resolveParameter(String input) {
            try {
                return input.split(" ")[1];
            } catch (ArrayIndexOutOfBoundsException exception) {
                return "";
            }
        }

        public Builder state(InputState state) {
            this.state = state;
            return this;
        }

        public InputEntity build() {
            InputEntity result = new InputEntity(this);
            result.setParam(resolveParameter(input));
            return result;
        }
    }
}

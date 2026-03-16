package com.molszewski.demos.poker.web.model.response;

public record ActionResponse(
        String myId,
        String errorMessage
) {
    public ActionResponse(String myId) {
        this(myId, null);
    }

    public static ActionResponse error(String errorMessage) {
        return new ActionResponse(null, errorMessage);
    }
}

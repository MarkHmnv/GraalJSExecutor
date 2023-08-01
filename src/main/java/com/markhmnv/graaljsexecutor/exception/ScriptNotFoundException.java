package com.markhmnv.graaljsexecutor.exception;

public class ScriptNotFoundException extends RuntimeException{
    public ScriptNotFoundException(){
        super("Script was not found");
    }
}

package com.markhmnv.graaljsexecutor.exception;

public class IllegalDeletionException extends RuntimeException{
    public IllegalDeletionException(){
        super("You can't delete running script");
    }
}

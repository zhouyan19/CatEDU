package com.example.catedu.data;

public class InstanceEnbedding {
    private InstanceWithUri instanceWithUri;
    private int start;
    private int end;

    public InstanceEnbedding(InstanceWithUri i, int s, int e) {
        instanceWithUri = i;
        start = s;
        end = e;
    }

    // getters
    public InstanceWithUri getInstanceWithUri () { return instanceWithUri; }
    public int getStart () { return start; }
    public int getEnd () { return end; }
}

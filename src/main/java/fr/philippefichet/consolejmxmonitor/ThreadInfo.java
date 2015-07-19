/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.philippefichet.consolejmxmonitor;

/**
 *
 * @author glopinous
 */
public class ThreadInfo {
    private Long id;
    private Long cpuTime;
    private String name;

    public ThreadInfo(Long id, Long cpuTime, String name) {
        this.id = id;
        this.cpuTime = cpuTime;
        this.name = name;
    }

    public Long getCpuTime() {
        return cpuTime;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }
    
}

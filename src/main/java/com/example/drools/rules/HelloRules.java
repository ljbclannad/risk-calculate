package com.example.drools.rules;

import com.example.drools.dto.Person;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

public class HelloRules {
    public static void main(String[] args) {
        KieServices ks = KieServices.Factory.get();
        KieContainer kContainer = ks.getKieClasspathContainer();
        KieSession kieSession = kContainer.newKieSession("hello");

        Person person = new Person();
        person.setName("testName");
        person.setAge(18);
        kieSession.insert(person);

        kieSession.setGlobal("count", 1);

        int count = kieSession.fireAllRules();


        System.out.println(kieSession.getGlobal("count"));
        System.out.println("Total number of rules executed: " + count);
        kieSession.dispose();
    }
}

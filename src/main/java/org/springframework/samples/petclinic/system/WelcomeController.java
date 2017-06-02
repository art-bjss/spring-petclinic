package org.springframework.samples.petclinic.system;


import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;

@Controller
class WelcomeController {

    @Autowired
    private SampleData sampleData;

    @RequestMapping(method = RequestMethod.GET, path = "/")
    public String welcome() {
        return "welcome";
    }

    @RequestMapping(method = RequestMethod.POST, path = "/")
//    @PostConstruct
    public String reset() {
        LoggerFactory.getLogger(getClass()).info("Resetting");
        sampleData.reset();
        return "redirect:/";
    }
}

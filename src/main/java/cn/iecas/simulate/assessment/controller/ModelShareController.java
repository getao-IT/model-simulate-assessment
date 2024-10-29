package cn.iecas.simulate.assessment.controller;


import cn.iecas.simulate.assessment.service.ModelShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



/**
 * @Time: 2024/10/29 11:23
 * @Author: guoxun
 * @File: ModelShareController
 * @Description:
 */
@RequestMapping("/modelShare")
@RestController
public class ModelShareController {
    
    @Autowired
    private ModelShareService modelShareService;


}

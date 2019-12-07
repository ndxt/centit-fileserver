package com.centit.fileserver.controller;

import io.swagger.annotations.Api;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 仅仅用于保存文件，可以用于没有权限要求的文件存储
 * 下载也没有权限限制
 */
@Controller
@RequestMapping("/store")
@Api(value = "文件断点上传，仅仅保存文件内容", tags = "文件断点上传，仅仅保存文件内容")
public class StoreFileController extends FileController {

}

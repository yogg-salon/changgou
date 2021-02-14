package com.changgou.search.controller;

import com.changgou.file.FastDFSFile;
import com.changgou.util.FastDFSUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value="/upload")
@CrossOrigin
public class FileUploadController {
    /**
     * 文件上传
     */
    @PostMapping
    public Result upload(@RequestParam(value="file")MultipartFile file){
        FastDFSFile fastDFSFile=null;
        //封装文件信息
        try {
             fastDFSFile=new FastDFSFile(file.getOriginalFilename(),
                    file.getBytes(),
                    StringUtils.getFilenameExtension(file.getOriginalFilename())
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        //调用FastDFSUtil工具类将文件传入FastDFS中
        String[] upload = FastDFSUtil.upload(fastDFSFile);
        String url= null;
        try {
            url = FastDFSUtil.getTrackerInfo()+"/"+upload[0]+"/"+upload[1];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new Result(true, StatusCode.OK,"上传成功",url);
    }



}

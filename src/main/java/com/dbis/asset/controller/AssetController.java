package com.dbis.asset.controller;

import com.dbis.asset.enums.CommonEnum;
import com.dbis.asset.exception.BizException;
import com.dbis.asset.mapper.AssetMapper;
import com.dbis.asset.pojo.Asset;
import com.dbis.asset.result.ResultBody;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName AssetController
 * @Description TODO
 * @Author tom
 * @Date 2019/10/18 17:52
 * @Version 1.0
 **/
@RestController
@RequestMapping("/asset")
public class AssetController {
    @Autowired
    private AssetMapper assetMapper;

    /**
     * 功能描述: //查找所有资产，并携带类别<br>
     * 〈〉
     * @Param: [pageNum, pageSize]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: tom
     * @Date: 2019/10/19 19:11
     */
    @GetMapping
    public ResultBody findAll(
            @RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum,
            @RequestParam(defaultValue = "10",value = "pageSize") Integer pageSize
                                           ){
        PageHelper.startPage(pageNum,pageSize);
        List<Asset> list = assetMapper.selectAllWithCategory();
        if(list.isEmpty()){
            throw  new BizException(CommonEnum.NOT_FOUND);
        }
        PageInfo<Asset> info = new PageInfo<>(list);
        return ResultBody.success(info);
    }




    /**
     * 功能描述: //模糊搜索<br>
     * 〈〉
     * @Param: [asset, pageNum, pageSize]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: tom
     * @Date: 2019/10/21 17:42
     */
    @GetMapping("/find")
    public ResultBody findByLike(@ModelAttribute Asset asset,
                                         @RequestParam(defaultValue = "1",value = "pageNum") Integer pageNum,
                                         @RequestParam(defaultValue = "10",value = "pageSize") Integer pageSize
                                         ){
        PageHelper.startPage(pageNum,pageSize);
        List<Asset> list = assetMapper.selectByLike(asset);
        if(list.isEmpty()){
            throw  new BizException(CommonEnum.NOT_FOUND);
        }
        PageInfo<Asset> info = new PageInfo<>(list);
        return ResultBody.success(info);
    }


    /**
     * 功能描述: //添加资产<br>
     * 〈〉
     * @Param: [asset]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: tom
     * @Date: 2019/10/21 17:42
     */
    @PostMapping
    public ResultBody addAsset(Asset asset){
        if(asset.getAssetName().isEmpty()){
            throw  new BizException(CommonEnum.BODY_NOT_MATCH.getResultCode(),"资产名不能为空！");
        }
        if(asset.getUserId()==null){
            throw  new BizException(CommonEnum.BODY_NOT_MATCH.getResultCode(),"用户userId不能为空！");
        }
        if(asset.getCateId()==null){
            throw  new BizException(CommonEnum.BODY_NOT_MATCH.getResultCode(),"类别cateId不能为空！");
        }
        if(assetMapper.insert(asset)!=1){
            throw  new BizException("-1","插入操作失败！");
        }else{
            return ResultBody.success();
        }
    }


    /**
     * 功能描述: //根据id删除资产信息<br>
     * 〈〉
     * @Param: [id]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: tom
     * @Date: 2019/10/21 17:42
     */
    @DeleteMapping("/{aid}")
    public ResultBody deleteAsset(@PathVariable int aid){
        int i = assetMapper.deleteByPrimaryKey(aid);
        if(i != 1){
            throw  new BizException("-1","删除操作失败！");
        }
        return ResultBody.success();
    }




    /**
     * 功能描述: //根据Id查找资产信息<br>
     * 〈〉
     * @Param: [id]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: tom
     * @Date: 2019/10/21 17:42
     */
    @GetMapping("/detail/{aid}")
    public ResultBody findAssetById(@PathVariable int aid){
        Asset data = assetMapper.selectByPrimaryKeyWithCategory(aid);
        if(data==null){
            throw new BizException(CommonEnum.NOT_FOUND);
        }
        return ResultBody.success(data);
    }



    /**
     * 功能描述: //更新资产信息<br>
     * 〈〉
     * @Param: [asset]
     * @Return: java.util.Map<java.lang.String,java.lang.Object>
     * @Author: tom
     * @Date: 2019/10/21 17:42
     */
    @PutMapping
    public ResultBody updateAsset(Asset asset){
        if(asset.getAid()==null){
            throw  new BizException(CommonEnum.BODY_NOT_MATCH.getResultCode(),"资产aid不能为空！");
        }
        int i = assetMapper.updateByPrimaryKey(asset);
        if( i !=1){
            throw  new BizException("-1","更新操作失败！");
        }

        return ResultBody.success();
    }

    @PostMapping("/batch")
    public Map<String, Object> handleFileUpload(HttpServletRequest request) {
        Map<String,  Object> map = new HashMap<>();
        List<MultipartFile> files = ((MultipartHttpServletRequest) request).getFiles("file");
        MultipartFile file = null;
        BufferedOutputStream stream = null;
        for (int i = 0; i < files.size(); ++i) {
            file = files.get(i);
//            "/webfile/downloads/" "E:/downloads/"
            String filePath = "E:/downloads/";
            if (!file.isEmpty()) {
                try {
                    byte[] bytes = file.getBytes();
                    String fileName =  System.currentTimeMillis() + file.getOriginalFilename();
                    filePath = filePath + fileName;
                    stream = new BufferedOutputStream(new FileOutputStream(
                            new File(filePath)));//设置文件路径及名字
                    map.put("url",fileName);
                    stream.write(bytes);// 写入
                    stream.close();

                } catch (Exception e) {
                    stream = null;
                    return map;
                }
            } else {
                return map;
            }
        }

        return map;
    }

    @GetMapping("/download/{aid}")
    public void downloadFile(@PathVariable String aid,HttpServletRequest request, HttpServletResponse response) {
        String fileName = request.getParameter("url");// 文件名
        String realPath = "E:/downloads/";// 文件名
        String[] type = fileName.split("\\.");
        System.out.println(fileName);

        if (fileName != null) {
            //设置文件路径
            File file = new File(realPath + fileName);
            if (file.exists()) {
                response.setContentType("image/" + type[1]);
                // 设置强制下载不打开
//                response.addHeader("Content-Disposition", "attachment;fileName=" + fileName);// 设置文件名
                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    OutputStream os = response.getOutputStream();
                    int i = bis.read(buffer);
                    while (i != -1) {
                        os.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

    }
}

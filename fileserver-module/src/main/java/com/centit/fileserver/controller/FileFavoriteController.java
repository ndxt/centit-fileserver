package com.centit.fileserver.controller;

import com.centit.fileserver.po.FileFavorite;
import com.centit.fileserver.service.FileFavoriteManager;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.WebOptUtils;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.core.dao.PageQueryResult;
import com.centit.support.database.utils.PageDesc;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * FileFavorite  Controller.
 * create by scaffold 2020-08-18 13:38:14
 * @author codefan@sina.com
 * 文件收藏
*/


@Controller
@RequestMapping("/favorite")
@Api(value = "FILE_FAVORITE", tags = "文件收藏")
public class FileFavoriteController  extends BaseController {

    private final FileFavoriteManager fileFavoriteMag;

    public FileFavoriteController(FileFavoriteManager fileFavoriteMag) {
        this.fileFavoriteMag = fileFavoriteMag;
    }

    /**
     * 查询所有   文件收藏  列表
     * @param request HttpServletRequest
     * @param pageDesc PageDesc
     * @return {data:[]}
     */
    @RequestMapping(method = RequestMethod.GET)
    @ApiOperation(value = "查询所有文件收藏列表")
    @WrapUpResponseBody
    public PageQueryResult<FileFavorite> list(HttpServletRequest request, PageDesc pageDesc) {
        Map<String, Object> searchColumn = collectRequestParameters(request);
        String topUnit = WebOptUtils.getCurrentTopUnit(request);
        List<FileFavorite> fileFavorites = fileFavoriteMag.listFileFavorite(topUnit,
            searchColumn, pageDesc);
        return PageQueryResult.createResult(fileFavorites,pageDesc);
    }

    /**
     * 查询单个  文件收藏

     * @param favoriteId  favorite_id
     * @return {data:{}}
     */
    @RequestMapping(value = "/{favoriteId}", method = {RequestMethod.GET})
    @ApiOperation(value = "查询单个文件收藏")
    @WrapUpResponseBody
    public FileFavorite getFileFavorite(@PathVariable String favoriteId) {

        return fileFavoriteMag.getFileFavorite( favoriteId);
    }

    /**
     * 新增 文件收藏
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param fileFavorite  {@link FileFavorite}
     */
    @RequestMapping(method = {RequestMethod.POST})
    @ApiOperation(value = "新增文件收藏")
    @WrapUpResponseBody
    public void createFileFavorite(@RequestBody FileFavorite fileFavorite, HttpServletRequest request,
                                   HttpServletResponse response) {
        if(StringUtils.isBlank(fileFavorite.getFavoriteUser())) {
            fileFavorite.setFavoriteUser(WebOptUtils.getCurrentUserCode(request));
        }
        fileFavoriteMag.createFileFavorite(fileFavorite);
        JsonResultUtils.writeSingleDataJson(fileFavorite,response);
    }

    /**
     * 删除单个  文件收藏

     * @param favoriteId  favorite_id
     */
    @RequestMapping(value = "/{favoriteId}", method = {RequestMethod.DELETE})
    @ApiOperation(value = "删除单个文件收藏")
    @WrapUpResponseBody
    public void deleteFileFavorite(@PathVariable String favoriteId) {

        fileFavoriteMag.deleteFileFavorite( favoriteId);


    }

    /**
     * 新增或保存 文件收藏

     * @param fileFavorite  {@link FileFavorite}
     */
    @RequestMapping(method = {RequestMethod.PUT})
    @ApiOperation(value = "更新文件收藏")
    @WrapUpResponseBody
    public void updateFileFavorite(@RequestBody FileFavorite fileFavorite) {
        fileFavoriteMag.updateFileFavorite(fileFavorite);
    }
}

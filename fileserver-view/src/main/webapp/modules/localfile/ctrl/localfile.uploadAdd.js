setI18NText("");
define(function(require) {
    var $ = require('jquery');
    var Config = require('config');
    var Page = require('core/page');
    var Dialog = require('centit/centit.dialog');
    require('myuploader');
    // 角色信息列表
    return Page.extend(function() {
        this.selectList;
        this.renderButton = function(){
            if(this.parent.isInTop){
                return false;
            }
            return true;
        };



        this.load = function (panel) {

            var vm = this;
            vm.showPath = this.parent.showPath;
            panel.find('input:radio[name=encryptType]').change(function() {
                vm.uploader.config.postVarsPerFile.encryptType = $(this).val();
            });

            panel.find('input:checkbox[name=index]').change(function() {
                vm.uploader.config.postVarsPerFile.index = this.checked;
            });

            panel.find('input:checkbox[name=pdf]').change(function() {
                var checked = this.checked;
                vm.uploader.config.postVarsPerFile.pdf = checked;
                panel.find('input:checkbox[name=watermark]').attr("disabled",!checked);
            });

            panel.find('input:checkbox[name=watermark]').change(function(){
                vm.uploader.config.postVarsPerFile.watermark = this.checked;
            });

            panel.find('input:checkbox[name=thumbnail]').change(function(){
                vm.uploader.config.postVarsPerFile.thumbnail = this.checked;
            });

            panel.find('input:password[name=pwd]').blur(function(){
                vm.uploader.config.postVarsPerFile.password = this.value;
            });

            panel.find('input:password[name=width]').blur(function(){
                vm.uploader.config.postVarsPerFile.width = this.value;
            });

            panel.find('input:password[name=height]').blur(function(){
                vm.uploader.config.postVarsPerFile.height = this.value;
            });

            //设置下载目录
            panel.find('input:text[name=path]').on('input',function() {
                var path = panel.find('input[name=path]')[0].value;
                if(!path){
                    vm.uploader.config.postVarsPerFile.filePath = vm.showPath[0] =="/" ? vm.showPath.substr(1) : vm.showPath;
                }
                else {
                    if(path[0]!=="/"){
                        alert("请以'/'开始");
                        panel.find('input[name=path]')[0].value = "";
                        return;
                    }
                    if(vm.showPath==""){
                        path = path.substr(1);
                    }
                    vm.uploader.config.postVarsPerFile.filePath =  vm.showPath[0] =="/" ? vm.showPath.substr(1) + path : vm.showPath + path;
                }
            });
            this.renderUploadBtn(panel);
            this.reload();
        };


        this.reload = function(){
            this.parent.reload();
        };



        this.renderUploadBtn = function(panel) {
            var vm = this;
            // "where FILE_UNIT = :uc and OS_ID='FILE_SVR' and OPT_ID='LOCAL_FILE' "
            var uploader = panel.find('#upload_btn').uploader({
                UploaderPath: Config.ContextPath,           // 文件服务器路径

                ViewContextPath: Config.ViewContextPath,    // 页面路径

                extFilters: [],

                /* 文件保存的信息 */
                info: {
                    filePath: !vm.showPath.indexOf('/') ? vm.showPath.substr(1) : vm.showPath,
                    osId: 'FILE_SVR',    // 业务系统ID
                    optId: 'LOCAL_FILE',      // 业务模块ID（对应的业务功能模块，比如 ：收件箱）
                    optMethod: 'test',      // 业务方法 （如果想在业务功能里细分，也可以作为子模块 来设置）
                    optTag: 'test',         // 业务的主键，用来反向定位到具体的业务，如果主键有多个，请用 url的参数方式编写，或者业务系统自己 解释
                    fileOwner:this.parent.userCode,       // 文件所属个人
                    fileUnit: this.parent.unitCode      // 文件所属单位
                },
                //完成回调
                onComplete : vm.reload,
                //上传成功后
                onQueueComplete : vm.uploadSuccess,
                //选择文件后
                onSelect : vm.uploadSelect,
                pretreatment: {
                    index: false,              // 是否加入全文检索
                    encryptType: '',    // 加密方式 zip：zipFile des:DES加密
                    password: "",  // 加密密码
                    pdf: false,               // 添加pdf 副本
                    watermark: false,            // 如果添加pdf副本 是否需要加水印，水印 文字
                    thumbnail: false,            // 添加缩略图
                    width: 100,        // 缩略图宽度
                    height: 100        // 缩略图高度
                }
            });

            this.uploader = uploader.data('uploader');
        };
        this.uploadSuccess = function(){

            this.selectList.forEach(function(val){
                var elementP = document.createElement('p');
                elementP.innerHTML = val.name + "文件已经上传成功";
                $('#uploInfoSuccess').append(elementP);
            })
        };
        this.uploadSelect = function(list){
            this.selectList = list;
            $('#uploInfo').html("");
        }
        // 重新剪裁图片
        // var file;
        // var reader = 1;
        // var k;
        // this.reloadImage = function(list,a){
        //     console.log(a);
        //     if(reader==1) {
        //         reader++;
        //         file = a;
        //         var image;
        //         reader = new FileReader();
        //         reader.onload = function () {
        //             image = new Image();
        //             // 通过 reader.result 来访问生成的 DataURL
        //             var url = reader.result;
        //             image.src = url;
        //             k = drawIntoCanvas(image,file,a);
        //         };
        //         reader.readAsDataURL(file);
        //     }
        //
        //     var drawIntoCanvas = function(image,file,a){
        //         var container = document.getElementById('canvasContainer');
        //         container.innerHTML = "";
        //         var canvas  = document.createElement('canvas');
        //         canvas.width = "400";
        //         canvas.height = "400";
        //         var ctx = canvas.getContext('2d');
        //         ctx.drawImage(image,0,0,400,400,0,0,image.width,image.height);
        //         container.appendChild(canvas);
        //         var data=canvas.toDataURL();
        //         var ia = new Uint8Array(data.length);
        //         for (var i = 0; i < data.length; i++) {
        //             ia[i] = data.charCodeAt(i);
        //         }
        //         var blob=new File(ia,a.name,{type:a.type});
        //         console.log(ia);
        //         return blob;
        //     };
        //
        //     if(!k){
        //         requestAnimationFrame(this.reloadImage);
        //     }
        //     else{
        //         reader = 0;
        //         return k;
        //     }
        // }
    });
});
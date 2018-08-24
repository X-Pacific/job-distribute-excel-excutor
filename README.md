# excel分布式处理组件的介绍
> 这是一个基于xxl-job的excel分布式处理组件，它可以自动拆分excel并通过xxl-job分布式定时任务的功能对大excel进行分布式计算和处理。

## 它帮你做了很多事情
1. 帮你自动拆分、读取excel文件数据，当然这些步骤对于开发人员是无感的
1. 帮你自动做分布式任务资源的争抢，当然这些分布式的步骤对于开发人员依然是无感的
1. 帮你自动统计主、子任务的执行时间以及百分比制的总进度
1. 帮你自动做主子一致性总数的校验
1. 而上面的一切仅仅需要开发人员动动手指加一行注解以及实现一个接口
1. 那么，开发人员可以完全把精力放在业务代码上，其他的事情交给框架去做吧

## 实现架构图

![image](https://note.youdao.com/yws/public/resource/71da29d880ebf4c2396c5bd7569299ae/xmlnote/B44C52ECBD0A4C4E9964966E9CA8B139/33201)

> 我们快来看看，怎么使用吧

# 快速开始

## 准备

1. 你需要先搭建一个xxl-job的环境，这个非常简单，具体参考：http://www.xuxueli.com/xxl-job/#/

2. 你要获得源码项目：**job-excutor**，这是个springboot项目，你当然可以把他轻松改造成springcloud项目，请联系我：zxpdt@163.com

3. 需要jdk1.8

4. elastic_job_excel_main表是定时任务操作主表，这个表由核心系统创建，核心系统关注的字段如下：
![image](https://note.youdao.com/yws/public/resource/71da29d880ebf4c2396c5bd7569299ae/xmlnote/EEDAD2EEAE6F43F795334DFAF0C0D5BE/33035)

5. elastic_job_excel_sub表是拆分excel任务子表，无需开发人员关注，框架自动处理

## 开发

1. 我们需要重点关注：com.sinosoft.jobexcutor.jobhandler这个包

> com.sinosoft.jobexcutor.jobhandler.handler xxl-job的任务入口类
> com.sinosoft.jobexcutor.jobhandler.callback 回调的类在这里
> com.sinosoft.jobexcutor.jobhandler.dealer 分布式excel处理类
> com.sinosoft.jobexcutor.jobhandler.split 拆分excel文件（这个不用管）

2. 我们现在开始添加一项任务的入口，并着手编写业务代码吧：

3. com.sinosoft.jobexcutor.jobhandler.handler包中存放的是xxl的handler，在这里创建一个类，类的头部所配置的@JobHandler(value="excelDistributedReadDemoJobHandler")是这个job的名字，这个名字需要配置到xxl-admin（注册中心）中。这个类需要注入一个dealer包中的处理服务，这个服务需要实现ExcelDistributedReadIntf<T>这个接口


```
/**
 *  这是分布式读取（处理）excel（csv）数据的入口例子
 *  1、必须继承IJobHandler
 *  2、需要调用ExcelDistributedReadIntf的实现类来实现业务处理
 *  3、可以（不是必须）在这里指定所有任务执行完成后的回调任务（实现ExcelDistributedCallBackIntf接口）
 *  4、ExcelDistributedReadIntf.deal方法入参shardingVO 在定时任务运行时必须为：ShardingUtil.ShardingVO shardingVO = ShardingUtil.getShardingVo();才可正常获取分片参数
 *  5、ExcelDistributedReadIntf.deal方法入参dealerCallBackInfo 请在本类中就进行实例化，框架会自动对这个类的成员变量进行赋值，dealerCallBackInfo对象存放的是一些你用的到的信息，具体看这个类的注释吧
 */
@JobHandler(value="excelDistributedReadDemoJobHandler")
@Component
public class ExcelDistributedReadDemoJobHandler extends IJobHandler {
    private final static Logger logger = LoggerFactory.getLogger(ExcelDistributedReadDemoJobHandler.class);
    @Autowired
    ExcelDistributedReadIntf<DemoJobDto> excelDistributedReadDemoJobDealer;
    @Autowired
    ExcelDistributedCallBackIntf excelDistributeReadDemoCallBack;

    @Override
    public ReturnT<String> execute(String s) {
        ShardingUtil.ShardingVO shardingVO = ShardingUtil.getShardingVo();
        DealerCallBackInfo<DemoJobDto> dealerCallBackInfo = new DealerCallBackInfo<DemoJobDto>();
        //回调也可以用下面的代码设置
        //dealerCallBackInfo.setExcelDistributedCallBackIntf(excelDistributeReadDemoCallBack);
        return excelDistributedReadDemoJobDealer.deal(shardingVO,dealerCallBackInfo);
    }
}
```

4. 在ExcelDistributedReadDemoJobDealer类重写的deal方法上需要添加一个ExcelDistributedRead注解


```
@Override
@ExcelDistributedRead(type = ExcelJobType.DEMO, isCheckSumAmount = true, clazz = DemoJobDto.class, callBackBeanName = ExcelJobType.DEMO_CALLBACK)
public ReturnT<String> deal(ShardingUtil.ShardingVO shardingVO,DealerCallBackInfo<DemoJobDto> dealerCallBackInfo) {
```

> ExcelDistributedRead注解是一些配置信息：


> - type 这是定时任务的任务类型，不同的任务需要不同，以标识当前这个定时任务的类型
> - isCheckSumAmount 这是自动校验主子表总数是否一致的开关，默认是true
> - clazz 返回读取的excel文件的模型对象类类型
> - callBackBeanName 回调类bean的名字，当然你可以不通过这种方式指定回调

> 上面的例子中标识当前的任务类型是ExcelJobType.DEMO（建议将类型统一于这个静态常量类中），自动校验主子数量一致，excel数据结构对应的pojo是DemoJobDto，以及回调方法的指定

> shardingVO对象固定传入即可，开发人员无需关心

> dealerCallBackInfo对象中的内容为：

> - hitFileName：命中子文件路径+名字，开发人员可以不用关心
> - uuid：命中主任务的uuid
> - errorInfo：可以把处理业务逻辑中的异常信息存入，框架自动处理异常
> - csvList：excel数据，已经通过泛型处理，，拿到直接遍历T类型即可。

> T类型需要在ExcelDistributedRead注解中配置，当pojo中的字段与excel的头部字段不一致时，可以通过CsvHead注解转化

```
//这个例子说明pojo中的字段名称为aaa，但对应excel里title的名称为bbb
@CsvHead("bbb")
private String aaa;
```

> 注意：请把pojo放入com.sinosoft.jobexcutor.csvdto包中


5. 随后就可以在方法内写业务逻辑了


```
@Override
@ExcelDistributedRead(type = ExcelJobType.DEMO, isCheckSumAmount = true, clazz = DemoJobDto.class, callBackBeanName = ExcelJobType.DEMO_CALLBACK)
public ReturnT<String> deal(ShardingUtil.ShardingVO shardingVO,DealerCallBackInfo<DemoJobDto> dealerCallBackInfo) {
    //处理csv，处理业务逻辑 begin
    List<DemoJobDto> csvList = dealerCallBackInfo.getCsvList();
    List<PrpCMainDemo> prpCMainDemos = new ArrayList<PrpCMainDemo>();
    for (int i = 0; csvList != null && i < csvList.size(); i++) {
        DemoJobDto dto = csvList.get(i);
        PrpCMainDemo prpCMainDemo = new PrpCMainDemo();
        BeanUtils.copyProperties(dto,prpCMainDemo);
        prpCMainDemos.add(prpCMainDemo);
    }
    try {
        demoJobService.saveAll(prpCMainDemos);
    } catch (Exception e) {
        //如果此处捕获异常，可以通过如下方式处理，否则请将异常抛出，框架自动处理
        //打印异常方式必须为：JobConstant.CSV_AOP_A1（阶段名称） + |UUID=?（有就显示）+ |需要打印的内容
        String errorInfo = JobConstant.CSV_CUSTOM + "|UUID=" + dealerCallBackInfo.getUuid() + "|保存文件“"+dealerCallBackInfo.getHitFileName()+"”异常："+e.getMessage();
        dealerCallBackInfo.setErrorInfo(errorInfo);
        logger.error(errorInfo,e);
    }
    //处理csv，处理业务逻辑 end
    return ReturnT.SUCCESS;
}
```

6. 回调类的示例如下，注意excelDistributeReadDemoCallBack是springbean的名字，需要与注解配置一致


```
/**
 * 回调类必须指定名字
 */
@Component("excelDistributeReadDemoCallBack")
public class ExcelDistributeReadDemoCallBack implements ExcelDistributedCallBackIntf {
    @Override
    public void callBack(Elastic_job_excel_main main) {
        System.out.println("我是回调哦["+main.getUuid()+"]");
    }
}

```


> 到这里就完成了编码的工作



---

> 下面是进阶功能文档


# 关于使用excel分布式处理组件的规范

> 1. 接入xxl-job的子任务入口必须放入：com.sinosoft.jobexcutor.jobhandler.handler包下
> 
> 1. handler调用的处理类（这里主要指使用本框架封装的分布式处理excel的方式）必须放入：com.sinosoft.jobexcutor.jobhandler.dealer，并且命名为*Dealer，同时需要实现ExcelDistributedReadIntf接口并使用ExcelDistributedRead注解，否则无法赋予分布式处理的能力
>  
> 1. 回调回写的处理类必须放入com.sinosoft.jobexcutor.jobhandler.callback包下，并实现ExcelDistributedCallBackIntf接口，否则无法自动回调
> 
> 1. 数据库实体类必须放入：com.sinosoft.jobexcutor.entity包，mybatis的mapper xml文件必须放入：com.sinosoft.jobexcutor.entity.xml包，这里推荐使用mybatis-generator:generate -X 插件自动生成基础类，对应配置文件位于：resources/mybatis-generator/generatorConfig.xml
> 
> 1. excel对应的实体类必须放入：com.sinosoft.jobexcutor.csvdto包中，并可以通过CsvHead注解来匹配excel表头的内容
>
> 1. 打印异常方式必须为：JobConstant.CSV_AOP_A1（阶段名称） + |UUID=?（有就显示）+ |需要打印的内容

# 配置xxl-job

> 先打开http://localhost:8080/job-admin/jobinfo注册中心

> 你需要先配置一个拆分excel的任务，你需要注意的是cron自定义定时间隔、JobHandler需要固定配置为ESJH，路由策略建议设置为“一致性hash”

![image](https://note.youdao.com/yws/public/resource/71da29d880ebf4c2396c5bd7569299ae/xmlnote/F9B363BAFFD64C9F88D523A65241C160/33087)


> 在来配置刚刚写好的任务，你需要注意的是路由策略必须设置为：分片广播、cron自定义定时间隔、JobHandler是你在handler类注解上定义的内容

![image](https://note.youdao.com/yws/public/resource/71da29d880ebf4c2396c5bd7569299ae/xmlnote/FACB0A0A545D4174A3404BC4115B669A/33101)

> 至此就完成全部的内容

# 非分布式处理excel

# 分布式锁
> 支持三种分布式锁的方式，如果没有redis、zk等基础设施，直接用DB即可

> 配置方法:

```
#DB REDIS ZK 分布式锁方式，默认为数据库（不推荐zk方式，推荐redis方式）
ExcelDistributedReadAop.distributedlock=DB

#redis的配置信息
spring.redis.hostName=localhost
spring.redis.port=6379
#redis.password=

#zk的配置信息 可以配置集群 ***:*,***:*
spring.zk.config.uri=localhost:2181
```
> 如果配置为zk或redis，需要配置zk和redis的信息


# 容器化部署

未完待续

# 异常流程处理

## 分布式异常处理流程

> 首选需要保证子任务有两个原则必须满足才可以使用分布式异常处理流程：任务需要可以重复执行、任务保证事务一致性。

> 当子任务处理异常时，需要下载失败的csv文件（可能稍后框架会提供下载接口），并结合“校验内容采集”章节的信息修改csv文件，修改后重新上传（可能稍后框架会提上传接口，不允许改名字），上传后请求如下url：

> http://localhost:8090/tools/dealfail/{uuid}/{fileserialno}

> 系统会直接重新执行这个子任务，执行完成如果成功，则和正常执行效果一致，可以根据框架异常处理方式与核心系统做运维功能结合


# 其他功能

## 自动校验
> 如果你想赋予excel数据读取自动校验的能力也非常简单，只需要在csv对应的pojo上添加如下注解即可：

```
@AutoCheck
public class DemoJobDto extends CsvBaseDto {
    @CsvHead("POLICYNO")
    @AutoCheckField(cname = "保单号",disUniq = true,notNull = true)
    private String policyno;
```

> @AutoCheck 注解开启自动校验功能

>  @AutoCheckField 配置了自动校验的内容，分别可以做如下几种自动校验：

> 1. boolean **disUniq**：如果字段上被此字段修饰，则被校验内容前缀会显示被注解的字段，以标识哪条数据有问题
> 1. boolean **notNull**：是否允许为空
> 1. String **select**：选择范围注解配置 空不校验 由于本框架限定，请配置时配置为 ,1,2,3,
> 1. boolean **isNotZero**：是否不能为0 空不校验 true不允许为0
> 1. AutoCheckFormat **fieldformat**：从下面枚举中选择格式：date,time,num,none 其中date格式为yyyy-mm-dd time格式为yyyy-mm-dd hh:mi:ss num格式为数字 none不做校验
> 1. int **length** 长度校验 为0时该项不可录入值 空不校验
> 1. String **cname** 字段默认汉字 校验返回使用

> 被校验的信息将会保存于elastic_job_excel_checkinfo表中，系统也针对校验信息做了一些查询异常的rest api接口，详见运维功能章节

## 手工校验框架

> 手工校验的异常信息可以直接通过入参DealerCallBackInfo中维护List<DealerCallBackErrorInfo<T>>列表即可

> 例子详见下面代码参考


```
    @Override
    @ExcelDistributedRead(type = ExcelJobType.DEMO, isCheckSumAmount = true, clazz = DemoJobDto.class, callBackBeanName = ExcelJobType.DEMO_CALLBACK)
    public ReturnT<String> deal(ShardingUtil.ShardingVO shardingVO,DealerCallBackInfo<DemoJobDto> dealerCallBackInfo) {
        //处理csv，处理业务逻辑
        List<DemoJobDto> csvList = dealerCallBackInfo.getCsvList();
        List<PrpCMainDemo> prpCMainDemos = new ArrayList<PrpCMainDemo>();

        List<DealerCallBackErrorInfo<DemoJobDto>> callBackErrorInfoList = new ArrayList<>();
        dealerCallBackInfo.setCheckInfoList(callBackErrorInfoList);
        for (int i = 0; csvList != null && i < csvList.size(); i++) {
            DemoJobDto dto = csvList.get(i);
            if(dto.getProposalno().equals("MOP08000046200")||
                    dto.getProposalno().equals("MOP0900079790000000")||
                    dto.getProposalno().equals("MOP08000082300")||
                    dto.getProposalno().equals("MOP090006858000")){
                DealerCallBackErrorInfo<DemoJobDto> checkinfo = new DealerCallBackErrorInfo<>();
                checkinfo.setT(dto);
                checkinfo.setErrorInfo("投保单号："+dto.getProposalno()+"有误");
                callBackErrorInfoList.add(checkinfo);
            }
            ……
```
## 校验内容采集

1. 主任务主键
1. 序号（与excel拆分序号对应）
1. excel拆分文件路径
1. 校验内容
1. 校验匹配码，同一批匹配码会一致
1. 被校验数据对应原excel行数
1. 被校验数据逻辑主键
1. 备用字段
1. 入表日期

> 采集到的校验信息可以帮助客户方便的定位到错误行以及原因


## 非分布式处理excel

## 拆分子任务自动转储

> elastic_job_excel_sub子任务表再任务完成后会自动转入elastic_job_excel_sub_his表中

## 分布式生成excel的功能……



# 运维功能

## 查看uuid的信息
> http://localhost:9090/tools/look/{uuid}


```
{
	"rop": "18%",--进度
	"uuid": "WINDOWS",
	"type": "DEMO",
	"riskcode": "0000",
	"excelpath": "*",
	"total": "93199",
	"status": "20",--excel处理状态 0未处理 |-1 excel拆分处理失败 1excel拆分完成  10excel拆分中|-2excel分布处理失败 2excel分布处理完成(有部分失败) 20excel分布处理中|100excel分布处理完成(全部成功)
	"allTimeUsed": 1234567890,--消耗的毫秒数
	"isLock": "0",--1已经锁定 0未锁定
	"errorinfo": "正常"
}
```

## 修复任务uuid至最原始状态（未拆分excel）
> http://localhost:8090/tools/repairone/{uuid}

## 修复任务uuid至最已经拆分excel但未开始分布式处理的状态
> http://localhost:8090/tools/repairtwo/{uuid}

## 解锁主任务uuid（zk暂不支持手动解锁）
> http://localhost:8090/tools/releaseLock/{uuid}

## 自动解锁超时锁（DB、REDIS）

> 此方法可以保障不会死锁（限于DB、REDIS方式）

> 配置以下定时任务即可：

> 任务描述：自动解超时基于数据库的分布式的锁 

> 定时：0/10 * * * * ? 

> JobHandler：deamonClearDeadLockJobHandler

> 路由：一致性哈希

## 查看某任务的最新校验信息

> http://localhost:8090/tools/newestcheckinfo/{uuid}/{serialno}
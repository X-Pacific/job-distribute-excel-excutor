# excel分布式处理组件的介绍
> 这是一个基于xxl-job的excel分布式处理组件，它可以自动拆分excel并通过xxl-job分布式定时任务的功能对大excel进行分布式计算和处理。

## 它做了什么
1. 帮你自动拆分、读取excel文件数据，当然这些步骤对于开发人员是无感的
1. 帮你自动做分布式任务资源的争抢，当然这些分布式的步骤对于开发人员依然是无感的
1. 帮你自动统计主、子任务的执行时间以及百分比制的总进度
1. 帮你自动做主子一致性总数的校验
1. 而上面的一切仅仅需要开发人员动动手指加一行注解以及实现一个接口
1. 开发人员可以完全把精力放在业务代码上，其他的事情框架去做

## 实现架构图

![image](http://pdv71y0dp.bkt.clouddn.com/TIM%E6%88%AA%E5%9B%BE20180914170154.png)


# STARTER

## 准备

1. 你需要先搭建一个xxl-job的环境，这个非常简单，具体参考：http://www.xuxueli.com/xxl-job/#/

2. 需要jdk1.8

3. elastic_job_excel_main表是定时任务操作主表，这个表由核心系统创建，核心系统关注的字段如下：
![image](http://pdv71y0dp.bkt.clouddn.com/1.png)

4. elastic_job_excel_sub表是拆分excel任务子表，无需开发人员关注，框架自动处理

## 开发

1. 我们需要重点关注：org.zxp.jobexcutor.jobhandler这个包

> org.zxp.jobexcutor.jobhandler.handler xxl-job的任务入口类
> org.zxp.jobexcutor.jobhandler.callback 回调的类在这里
> org.zxp.jobexcutor.jobhandler.dealer 分布式excel处理类
> org.zxp.jobexcutor.jobhandler.split 拆分excel文件（这个不用管）

2. 我们现在开始添加一项任务的入口，并着手编写业务代码吧：

3. org.zxp.jobexcutor.jobhandler.handler包中存放的是xxl的handler，在这里创建一个类，类的头部所配置的@JobHandler(value="excelDistributedReadDemoJobHandler")是这个job的名字，这个名字需要配置到xxl-admin（注册中心）中。这个类需要注入一个dealer包中的处理服务，这个服务需要实现ExcelDistributedReadIntf<T>这个接口


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

> 注意：请把pojo放入org.zxp.jobexcutor.csvdto包中


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

> 1. 接入xxl-job的子任务入口必须放入：org.zxp.jobexcutor.jobhandler.handler包下
> 
> 1. handler调用的处理类（这里主要指使用本框架封装的分布式处理excel的方式）必须放入：org.zxp.jobexcutor.jobhandler.dealer，并且命名为*Dealer，同时需要实现ExcelDistributedReadIntf接口并使用ExcelDistributedRead注解，否则无法赋予分布式处理的能力
>  
> 1. 回调回写的处理类必须放入org.zxp.jobexcutor.jobhandler.callback包下，并实现ExcelDistributedCallBackIntf接口，否则无法自动回调
> 
> 1. 数据库实体类必须放入：org.zxp.jobexcutor.entity包，mybatis的mapper xml文件必须放入：org.zxp.jobexcutor.entity.xml包，这里推荐使用mybatis-generator:generate -X 插件自动生成基础类，对应配置文件位于：resources/mybatis-generator/generatorConfig.xml
> 
> 1. excel对应的实体类必须放入：org.zxp.jobexcutor.csvdto包中，并可以通过CsvHead注解来匹配excel表头的内容
>
> 1. 打印异常方式必须为：JobConstant.CSV_AOP_A1（阶段名称） + |UUID=?（有就显示）+ |需要打印的内容

# 配置xxl-job

> 先打开http://localhost:8080/job-admin/jobinfo注册中心

> 你需要先配置一个拆分excel的任务，你需要注意的是cron自定义定时间隔、JobHandler需要固定配置为ESJH，路由策略建议设置为“一致性hash”

![image](http://pdv71y0dp.bkt.clouddn.com/2.png)


> 在来配置刚刚写好的任务，你需要注意的是路由策略必须设置为：分片广播、cron自定义定时间隔、JobHandler是你在handler类注解上定义的内容

![image](http://pdv71y0dp.bkt.clouddn.com/3.png)

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

> 注意：本例是在centos7下操作，不同操作系统请更换命令

> 容器化部署有两个特别需要注意的地方，否则可能造成无法正常调度任务


1. docker容器中的时间，linux的时间都需要保持一致，具体方法如下：

```
yum install -y ntpdate
ntpdate time.windows.com
```

2. 如果通过docker方式启动，需要在调度中心执行器管理界面，将执行器编辑页面中的机器地址设置为你容器集群中的ip:port，多个以逗号隔开，否则无法正常调度

## 第一部分，公共服务部分

### 清理docker容器，以及关闭防火墙

```
systemctl stop firewalld.service
systemctl restart docker
```

### docker安装mysql（配合调度中心所用）

```
//拉区mysql镜像
docker pull hub.c.163.com/library/mysql
//重命名镜像名称
docker tag IMAGEID(镜像id) mysql
//启动mysql容器，并将数据库文件目录挂载宿主机，指定root用户密码
docker run --privileged=true --name mysql5.7 -p 3306:3306 -v /my/mysql/datadir:/var/lib/mysql -v /my/mysql/conf.d:/etc/mysql/conf.d -e MYSQL_ROOT_PASSWORD=123456 -d mysql:5.7
```

> mysql建设好后，需要执行脚本，[点击下载](http://pdv71y0dp.bkt.clouddn.com/xxl-job.sql/)



### docker安装redis

```
docker pull  redis:3.2
docker run -d -p 6379:6379 docker.io/redis:3.2
```

### 通过dockerfile制作调度中心镜像

> 将job-admin.zip拷贝到与dockerfile同一路径

> dockerfile内容如下：


```
from hub.c.163.com/library/tomcat:latest
MAINTAINER zxp
ENV DIR_WEBAPP /usr/local/tomcat/webapps/
RUN  rm -rf $DIR_WEBAPP/*
ADD job-admin.zip $DIR_WEBAPP/job-admin.zip
RUN unzip $DIR_WEBAPP/job-admin.zip  -d  $DIR_WEBAPP/
CMD ["catalina.sh", "run"]
```
> 执行生成镜像的命令


```
docker build -t job-admin:latest . 
```
> 启动调度中心的容器

```
docker run -d -p 8080:8080 job-admin
```

> job-admin.zip中需要将配置文件中的xxl.job.db参数调整为docker的mysql实例地址，xxl.job.mail需要设置为通知邮箱

## 第二部分，执行任务单元部分

> 先把构建好的job-excutor-0.0.1-SNAPSHOT.jar与jdk1.8放入同一目录

> 需要制定多个dockerfile，命名一个文件为9090（对应9090端口），内容为：

> 编码部分的设定是为了让容器日志不乱码，jdk1.8.0_131是同级目录jdk的名字

```
FROM centos
MAINTAINER zxp
COPY jdk1.8.0_131 jdk1.8.0_131
ADD job-excutor-0.0.1-SNAPSHOT.jar app.jar
ENV LANG en_US.UTF-8  
ENV LANGUAGE en_US:en  
ENV LC_ALL en_US.UTF-8
ENV JAVA_HOME=/jdk1.8.0_131
ENV PATH=$JAVA_HOME/bin:$PATH
ENV CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
EXPOSE 9090 9000
ENTRYPOINT ["java","-Xms1024m","-Xmx1024m","-Dserver.port=9090","-Dxxl.job.executor.port=9000","-jar","/app.jar"]
```

> 在命名一个文件未9091（对应9091端口），内容为：


```
FROM centos
MAINTAINER zxp
COPY jdk1.8.0_131 jdk1.8.0_131
ADD job-excutor-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_HOME=/jdk1.8.0_131
ENV PATH=$JAVA_HOME/bin:$PATH
ENV CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib/tools.jar
ENV LANG en_US.UTF-8  
ENV LANGUAGE en_US:en  
ENV LC_ALL en_US.UTF-8
EXPOSE 9091 9001
ENTRYPOINT ["java","-Xms1024m","-Xmx1024m","-Dserver.port=9091","-Dxxl.job.executor.port=9001","-jar","/app.jar"]
```

> 如果你有更多的节点，按照这种方式做dockerfile即可

> 然后在创建一个生成镜像的shell文件，并放如同级目录（同理，多个实例也需要调整这个sh文件）：

```
rm -rf dockerfile
cp 9090 dockerfile
docker build -t job9090:latest .
rm -rf dockerfile
cp 9091 dockerfile
docker build -t job9091:latest .
```

> 最后启动任务执行器容器，注意这里的目录挂载


```
docker run --privileged=true -d -v /root/excel:/root/excel -p 9090:9090 -p 9000:9000 job9090
docker run --privileged=true -d -v /root/excel:/root/excel -p 9091:9091 -p 9001:9001 job9091
```

## 第三部分，docker容器日志与elk结合方案

> 参照http://dockone.io/article/2252


### 安装docker-compose

```
curl -L https://github.com/docker/compose/releases/download/1.21.2/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose

chmod +x /usr/local/bin/docker-compose

docker-compose --version
```
### 搭建elasticstack

> 我这里使用的版本是5.6.11

> 这里没有使用docker版本的elk，主要的原因：考虑使用已有elk基础设施，另一方面，自己搭建更加灵活

> elasticsearch搭建、kibana正常搭建即可

> logstash搭建需要注意的是：logstash.conf需要指定tcp插件接收docker日志驱动发送的sockit日志，这里的5000是接收日志信息的端口

```
input {
   tcp {
     port => 5000
   }
}

filter {
}

output {
  elasticsearch {
  	hosts=>"192.168.0.147:9200"
  	index => "docker-%{+YYYY.MM.dd}"
  }
  stdout{ }
}
```

> 启动logstash```nohup sh logstash -f ../config/logstash.conf &```

> 启动docker容器需要调整参数：

> --log-driver=syslog 是以syslog方式打印日志

> --log-opt syslog-address=tcp://192.168.0.147:5000 将日志输出到IP:PORT上，这里就是logstash开放的端口

```
docker run --log-driver=syslog \
 --log-opt syslog-address=tcp://192.168.0.147:5000 \
 --privileged=true -d  -v /root/excel:/root/excel -p 9090:9090 -p 9000:9000 job9090
```

> 如果此处logstash发生大量阻塞情况，可以考虑在elasticstack中添加kafka作为HA方案，这里我列出双Logstash的配置作为参考


```
#L1
input {
   tcp {
     port => 5000
   }
}

filter {
}

output {
  kafka {
 		bootstrap_servers => "192.168.0.147:9092,192.168.0.147:9093"
		topic_id => "dockerTopic"
  }
}
#L2
input {
   kafka {
     bootstrap_servers => "192.168.0.147:9092,192.168.0.147:9093"
     topics => ["dockerTopic"]
  	 auto_offset_reset => "latest"
  	 consumer_threads => 5
   }
}

filter {
}

output {
  elasticsearch {
  	hosts=>"192.168.0.147:9200"
  	index => "dok-%{+YYYY.MM.dd}"
  }
  stdout{ }
}
```




> 这里也可以使用下面方式启动，docker容器的日志将以json文件的形式存放在硬盘中，位于：/var/lib/docker/containers/容器ID/容器ID-json.log

```
docker run --log-driver=json-file --log-opt syslog-address=tcp:192.168.0.147:5000 --privileged=true -d  -v /root/excel:/root/excel -p 9090:9090 -p 9000:9000 job9090
```
> 通过这种方式可以通过filebeat做文件传递，只是比较麻烦，还需要单独做一个filebeak的容器，不推荐



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

> 非分布式处理excel主任务表elastic_job_excel_main.extfield2需要为0，配置方式和分布式方式一致，其中个中所引用的类不一致


```
@Component("excelReadDemoJobDealer")
//注意这里需要实现ExcelReadIntf接口
public class ExcelReadDemoJobDealer  implements ExcelReadIntf {
    private final static Logger logger = LoggerFactory.getLogger(ExcelReadDemoJobDealer.class);
    @Autowired
    private DemoJobService demoJobService;

    @Override
    @ExcelRead(type = ExcelJobType.NDEMO, clazz = DemoJobDto.class)
    //注意这里需要使用ExcelRead注解
    public ReturnT<String> deal(DealerCallBackInfo dealerCallBackInfo) {
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
        return ReturnT.SUCCESS;
    }
}
```


## 拆分子任务自动转储

> elastic_job_excel_sub子任务表再任务完成后会自动转入elastic_job_excel_sub_his表中




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

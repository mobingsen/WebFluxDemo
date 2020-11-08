package com.mbs.webflux.demo;

/**
 * 响应式编程（reactive programming）是一种基于数据流（data stream）和变化传递（propagation of change）的声明式（declarative）的编程范式
 *
 * 在命令式编程(我们的日常编程模式)下，式子a=b+c，这就意味着a的值是由b和c计算出来的。如果b或者c后续有变化，不会影响到a的值
 * 在响应式编程下，式子a:=b+c，这就意味着a的值是由b和c计算出来的。但如果b或者c的值后续有变化，会影响到a的值
 * 我认为上面的例子已经可以帮助我们理解变化传递（propagation of change）
 *
 * Java 平台直到 JDK 9才提供了对于Reactive的完整支持，JDK9也定义了上述提到的四个接口，在java.util.concurrent包上
 * 数据来源，一般称为生产者（Producer）
 * 数据的目的地，一般称为消费者(Consumer)
 * 在处理时，对数据执行某些操作一个或多个处理阶段。（Processor)
 * 到这里我们再看回响应式流的接口，我们应该就能懂了：
 *
 * Publisher（发布者)相当于生产者(Producer)
 * Subscriber(订阅者)相当于消费者(Consumer)
 * Processor就是在发布者与订阅者之间处理数据用的
 * 在响应式流上提到了back pressure（背压)这么一个概念，其实非常好理解。在响应式流实现异步非阻塞是基于生产者和消费者模式的，而生产者消费者很容易出现的一个问题就是：生产者生产数据多了，就把消费者给压垮了。
 *
 * 而背压说白了就是：消费者能告诉生产者自己需要多少量的数据。这里就是Subscription接口所做的事。
 * class MyProcessor extends SubmissionPublisher<String>
 *         implements Processor<Integer, String> {
 *
 *     private Subscription subscription;
 *
 *     @Override
 *     public void onSubscribe(Subscription subscription) {
 *         // 保存订阅关系, 需要用它来给发布者响应
 *         this.subscription = subscription;
 *
 *         // 请求一个数据
 *         this.subscription.request(1);
 *     }
 *
 *     @Override
 *     public void onNext(Integer item) {
 *         // 接受到一个数据, 处理
 *         System.out.println("处理器接受到数据: " + item);
 *
 *         // 过滤掉小于0的, 然后发布出去
 *         if (item > 0) {
 *             this.submit("转换后的数据:" + item);
 *         }
 *
 *         // 处理完调用request再请求一个数据
 *         this.subscription.request(1);
 *
 *         // 或者 已经达到了目标, 调用cancel告诉发布者不再接受数据了
 *         // this.subscription.cancel();
 *     }
 *
 *     @Override
 *     public void onError(Throwable throwable) {
 *         // 出现了异常(例如处理数据的时候产生了异常)
 *         throwable.printStackTrace();
 *
 *         // 我们可以告诉发布者, 后面不接受数据了
 *         this.subscription.cancel();
 *     }
 *
 *     @Override
 *     public void onComplete() {
 *         // 全部数据处理完了(发布者关闭了)
 *         System.out.println("处理器处理完了!");
 *         // 关闭发布者
 *         this.close();
 *     }
 *
 * }
 *
 * public class FlowDemo2 {
 *
 *     public static void main(String[] args) throws Exception {
 *         // 1. 定义发布者, 发布的数据类型是 Integer
 *         // 直接使用jdk自带的SubmissionPublisher
 *         SubmissionPublisher<Integer> publiser = new SubmissionPublisher<Integer>();
 *
 *         // 2. 定义处理器, 对数据进行过滤, 并转换为String类型
 *         MyProcessor processor = new MyProcessor();
 *
 *         // 3. 发布者 和 处理器 建立订阅关系
 *         publiser.subscribe(processor);
 *
 *         // 4. 定义最终订阅者, 消费 String 类型数据
 *         Subscriber<String> subscriber = new Subscriber<String>() {
 *
 *             private Subscription subscription;
 *
 *             @Override
 *             public void onSubscribe(Subscription subscription) {
 *                 // 保存订阅关系, 需要用它来给发布者响应
 *                 this.subscription = subscription;
 *
 *                 // 请求一个数据
 *                 this.subscription.request(1);
 *             }
 *
 *             @Override
 *             public void onNext(String item) {
 *                 // 接受到一个数据, 处理
 *                 System.out.println("接受到数据: " + item);
 *
 *                 // 处理完调用request再请求一个数据
 *                 this.subscription.request(1);
 *
 *                 // 或者 已经达到了目标, 调用cancel告诉发布者不再接受数据了
 *                 // this.subscription.cancel();
 *             }
 *
 *             @Override
 *             public void onError(Throwable throwable) {
 *                 // 出现了异常(例如处理数据的时候产生了异常)
 *                 throwable.printStackTrace();
 *
 *                 // 我们可以告诉发布者, 后面不接受数据了
 *                 this.subscription.cancel();
 *             }
 *
 *             @Override
 *             public void onComplete() {
 *                 // 全部数据处理完了(发布者关闭了)
 *                 System.out.println("处理完了!");
 *             }
 *
 *         };
 *
 *         // 5. 处理器 和 最终订阅者 建立订阅关系
 *         processor.subscribe(subscriber);
 *
 *         // 6. 生产数据, 并发布
 *         publiser.submit(-111);
 *         publiser.submit(111);
 *
 *         // 7. 结束后 关闭发布者
 *         // 正式环境 应该放 finally 或者使用 try-resouce 确保关闭
 *         publiser.close();
 *
 *         // 主线程延迟停止, 否则数据没有消费就退出
 *         Thread.currentThread().join(1000);
 *     }
 *
 * }
 *
 * Java 8 的 Stream 主要关注在流的过滤，映射，合并，而 Reactive Stream 更进一层，侧重的是流的产生与消费，即流在生产与消费者之间的协调
 * 说白了就是：响应式流是异步非阻塞+流量控制的(可以告诉生产者自己需要多少的量/取消订阅关系)
 *
 * 展望响应式编程的场景应用：
 *
 * 比如一个日志监控系统，我们的前端页面将不再需要通过“命令式”的轮询的方式不断向服务器请求数据然后进行更新，而是在建立好通道之后，数据流从系
 * 统源源不断流向页面，从而展现实时的指标变化曲线；
 * 再比如一个社交平台，朋友的动态、点赞和留言不是手动刷出来的，而是当后台数据变化的时候自动体现到界面上的。
 *
 * Created by 小墨 on 2020/11/8 17:09
 */
public class Note {
}

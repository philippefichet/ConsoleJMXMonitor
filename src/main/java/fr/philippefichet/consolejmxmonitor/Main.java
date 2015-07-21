package fr.philippefichet.consolejmxmonitor;

import com.googlecode.lanterna.TerminalFacade;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.Terminal;
import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import fr.philippefichet.ProgressBar;
import java.io.File;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author glopinous
 */
public class Main {
    public static void main(String[] args) throws MalformedURLException, IOException, InstanceNotFoundException, IntrospectionException, ReflectionException, MalformedObjectNameException, InterruptedException, AgentLoadException, AttachNotSupportedException, AgentInitializationException {
        if(args.length > 0) {
            System.err.println("Use -Dxxx : ");
            System.err.println("\t-Djmx.service.host=localhost");
            System.err.println("\t-Djmx.service.port=9990");
            System.err.println("\t-Djmx.service.url=service:jmx:remote+http://localhost:9990");
            System.err.println("\t-Djmx.service.login (default: no credentials)");
            System.err.println("\t-Djmx.service.password (default: no credentials)");
            System.err.println("\t-Djmx.service.pid (default: use jmx.service.url)");
            System.exit(1);
        }
        List<Pattern> threadNameExclude = new ArrayList<>();
        String pid = System.getProperty("jmx.service.pid");
        Map<String, Object> env = new HashMap<>();
        JMXConnector jmxConnector = null;
        String urlString = null;
        if (pid == null) {
            String host = System.getProperty("jmx.service.host", "localhost");
            int port = Integer.parseInt(System.getProperty("jmx.service.port", "9990"));
            urlString = 
                System.getProperty("jmx.service.url", "service:jmx:remote+http://" + host + ":" + port);
            String login = System.getProperty("jmx.service.login");
            String password = System.getProperty("jmx.service.password");
            String[] creds = {login, password};
            env.put(JMXConnector.CREDENTIALS, creds);
        } else {
            // attach to target VM
            VirtualMachine vm = VirtualMachine.attach(pid);
            
            urlString = vm.getAgentProperties().getProperty(
                "com.sun.management.jmxremote.localConnectorAddress"
            );
            if (urlString == null) {
              String agent = vm.getSystemProperties().getProperty(
                "java.home")+File.separator+"lib"+File.separator+
                "management-agent.jar";
              vm.loadAgent(agent);
              urlString = vm.getAgentProperties().getProperty(
                "com.sun.management.jmxremote.localConnectorAddress");
            }
        }
        JMXServiceURL serviceURL = new JMXServiceURL(urlString);
        jmxConnector = JMXConnectorFactory.connect(serviceURL, env);
        MBeanServerConnection connection = jmxConnector.getMBeanServerConnection();

//        System.out.println("committed : " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getCommitted());
//        Long committed = ManagementFactory.getPlatformMXBean(connection, MemoryMXBean.class).getHeapMemoryUsage().getCommitted();
//        System.out.println("committed = " + committed);
//        
//        connection.queryNames(null, null).stream().filter((b) -> b.toString().toLowerCase().contains("mem")).forEach((b) -> {
//            System.out.println("b = " + b);
//        });
//        MBeanInfo mBeanInfo = connection.getMBeanInfo(ObjectName.getInstance("java.lang:type=Memory"));
//        System.out.println("mBeanInfo = " + mBeanInfo);
//        //java.lang.management.MemoryUsage;
//        for (MBeanAttributeInfo attribute : mBeanInfo.getAttributes()) {
//            System.out.println(attribute.getName());
//            
//            for (String fieldName : attribute.getDescriptor().getFieldNames()) {
//                Object value = attribute.getDescriptor().getFieldValue(fieldName);
//                System.out.println("\t" + fieldName + " = " + attribute.getDescriptor().getFieldValue(fieldName).getClass());
//                if (value instanceof CompositeType) {
//                    CompositeType compositeType = (CompositeType)value;
//                    compositeType.keySet().forEach(v -> {
//                        System.out.println("\t\t" + v + " = " + ((SimpleType)compositeType.getType(v)));
//                        System.out.println("\t\t" + v + " = " + compositeType.getType(v));
//                    });
//                }
////                if ("javax.management.openmbean.CompositeType".equals(attribute.getType())) {
////                    ((CompositeType)attribute.getDescriptor().getFieldValue(fieldName)).keySet().forEach(v -> {
////                        System.out.println("\t\t -  = " + v);
////                    });
////                }
//            }
//        }
        
        Terminal terminal = null;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            terminal = TerminalFacade.createCygwinTerminal();
        } else {
            terminal = TerminalFacade.createUnixTerminal();
        }
        terminal.setCursorVisible(false);
        Screen screen = TerminalFacade.createScreen(terminal);
        screen.startScreen();
        
        
        int unit = 1024;
        String unitString = "Ko";
        int maxThreadNameOld = 0 ;
        int maxThreadView = 5;
        boolean wait = true;
        while(wait) {
            int row = 0;
            List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getPlatformMXBeans(connection, GarbageCollectorMXBean.class);
            MemoryMXBean memoryMXBean = ManagementFactory.getPlatformMXBean(connection, MemoryMXBean.class);
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            Long usedHeap = heapMemoryUsage.getUsed();
            Long maxHeap = heapMemoryUsage.getMax();
            screen.putString(0, row++, new Date().toString(), null, null);
            for (GarbageCollectorMXBean garbageCollectorMXBean : garbageCollectorMXBeans) {
                screen.putString(0, row++, "GC[" + garbageCollectorMXBean.getObjectName() +"] " + garbageCollectorMXBean.getCollectionCount() + " " + "/" + garbageCollectorMXBean.getCollectionTime() + " ms.", null, null);
            }
            String heapString = "Heap " + usedHeap/unit + " " + unitString + "/" + maxHeap/unit + " " + unitString;
            ProgressBar progressBarHeap = new ProgressBar(usedHeap, maxHeap, terminal.getTerminalSize().getColumns() - heapString.length() - 1);
            screen.putString(heapString.length() + 1, row, progressBarHeap.getProgressBar(), null, null);
            screen.putString(0, row++, heapString, null, null);
            
            ThreadMXBean threadMXBean = ManagementFactory.getPlatformMXBean(connection, ThreadMXBean.class);
            List<ThreadInfo> threads = new ArrayList<>();
            long maxThreadName = 0;
            long maxThreadCpuTime = 0;
            for (long threadId : threadMXBean.getAllThreadIds()) {
                long cpuTime = threadMXBean.getThreadCpuTime(threadId);
                // Ne prend que les thread ayant assez consommé de CPU (> 100ms)
                if (cpuTime > 100000000) {
                    String name = threadMXBean.getThreadInfo(threadId).getThreadName();
                    boolean exclude = false;
                    for (Pattern threadNameExclude1 : threadNameExclude) {
                        Matcher matcher = threadNameExclude1.matcher(name);
                        if (matcher.find()) {
                            exclude = true;
                            break;
                        }
                    }
                    if (!exclude) {
                        maxThreadCpuTime = Math.max(maxThreadCpuTime, (long)Math.log10(cpuTime)-5);
                        threads.add(new ThreadInfo(threadId, cpuTime, name));
                    }
                }
            }
            
            threads.sort(new Comparator<ThreadInfo>() {
                @Override
                public int compare(ThreadInfo o1, ThreadInfo o2) {
                    return o2.getCpuTime().compareTo(o1.getCpuTime());
                }
            });
            threads = threads.subList(0, Math.min(threads.size(), maxThreadView));
            for (ThreadInfo thread : threads) {
                maxThreadName = Math.max(maxThreadName, thread.getName().length());
            }
            
            for (ThreadInfo thread : threads) {
                screen.putString(2, row, String.format("%"+ maxThreadName +"s = %" + maxThreadCpuTime + "d ms", thread.getName(), (long)(thread.getCpuTime()/1000000)), null, null);
                row++;
            }
            
            if (maxThreadNameOld > maxThreadName) {
                screen.completeRefresh();
            } else {
                screen.refresh();
            }
            Key k = terminal.readInput();
            if (k!= null) {
                if(k.isCtrlPressed() && k.getCharacter() == 'c') {
                    wait = false;
                }
            }
            Thread.sleep(1000);
        }
    }
}

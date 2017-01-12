/**
 * 
 */
package gov.nih.nci.coppa.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.lang.SystemUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

/**
 * Dumps a whole bunch of useful debugging info about the application and
 * container into the log.
 * 
 * @author dkrylov
 * @see RequestTrackingFilter
 */
// CHECKSTYLE:OFF
@SuppressWarnings("PMD")
public class ComprehensiveApplicationStatusLogger {

    private static final String CRLF = SystemUtils.LINE_SEPARATOR;
    private static final Logger LOG = Logger
            .getLogger(ComprehensiveApplicationStatusLogger.class);

    private static final List<String> MBEANS = Arrays.asList(
            "com.mchange.v2.c3p0:type=PooledDataSource",
            "type=GarbageCollector", "java.lang:type=Memory",
            "type=MemoryPool", "java.lang:type=OperatingSystem",
            "java.lang:type=Runtime", "jboss.as:data-source=",
            "subsystem=ejb3", "subsystem=web", "jboss.as:statistics=jdbc",
            "jboss.as:statistics=pool", "jboss.as:subsystem=transactions",
            "statistics=pool,subsystem=datasources",
            "jboss.as.expr:statistics=jdbc", "jboss.as.expr:statistics=pool"

    );

    /**
     * @return String
     * @throws Exception
     *             Exception
     */
    public String execute() throws Exception { // NOPMD
        LOG.error("********** Beginning a comprehensive application/container status dump");

        dumpThreads();
        dumpRequestProcessingInfo();
        dumpMemory();
        dumpCPU();
        dumpDiskInfo();
        dumpMBeans();
        dumpDatabaseInfo();
        dumpOSCommands();

        LOG.error("********** End of a comprehensive application/container status dump");
        return "success";
    }

    @SuppressWarnings("unchecked")
    private void dumpMBeans() {
        LOG.error("MBeans:");
        StringBuilder sb = new StringBuilder();
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> names = new TreeSet<ObjectName>(server.queryNames(
                    null, null));
            for (ObjectName name : names) {
                if (!allowedToLog(name)) {
                    continue;
                }
                sb.append("MBean: " + name.getCanonicalName() + CRLF);
                try {
                    MBeanInfo mbeanInfo = server.getMBeanInfo(name);
                    for (int i = 0; i < mbeanInfo.getAttributes().length; i++) {
                        String attributeName = mbeanInfo.getAttributes()[i]
                                .getName();
                        String attributeValue = server.getAttribute(name,
                                attributeName).toString();
                        sb.append(attributeName + "=" + attributeValue + CRLF);
                    }
                } catch (Exception e) {
                    LOG.error(e.toString());
                }
                sb.append(CRLF);
            }
        } catch (Exception e) {
            LOG.error(e);
        }
        LOG.error(sb);

    }

    private boolean allowedToLog(ObjectName name) {
        for (String namePart : MBEANS) {
            if (name.getCanonicalName().contains(namePart)) {
                return true;
            }
        }
        return false;
    }

    private void dumpOSCommands() {
        dumpOSCommand("top -bM -d 0.5 -n 20");
        dumpOSCommand("free -mlta");
        dumpOSCommand("df -h");
        dumpOSCommand("vmstat -a -S M 1 15");
        dumpOSCommand("sar -W");
    }

    private void dumpOSCommand(String cmd) {
        LOG.error("Executing " + cmd + ":");
        StringBuilder sb = new StringBuilder();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(out, err);

            CommandLine cmdLine = CommandLine.parse(cmd);
            DefaultExecutor executor = new DefaultExecutor();
            ExecuteWatchdog watchdog = new ExecuteWatchdog(30000);
            executor.setWatchdog(watchdog);
            executor.setStreamHandler(streamHandler);
            int exitValue = executor.execute(cmdLine);

            sb.append(CRLF);
            sb.append("Exit code: " + exitValue + CRLF);
            sb.append("stdout: " + CRLF + out + CRLF);
            sb.append("stderr: " + CRLF + err + CRLF);
            LOG.error(sb);
        } catch (Exception e) {
            LOG.error(e);
        }

    }

    private void dumpCPU() {
        LOG.error("CPU Info:");
        try {
            OperatingSystemMXBean osBean = ManagementFactory
                    .getOperatingSystemMXBean();
            LOG.error(BeanUtils.describe(osBean));
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private void dumpMemory() {
        LOG.error("Memory Info:");
        Runtime runtime = Runtime.getRuntime();
        NumberFormat format = NumberFormat.getInstance();

        StringBuilder sb = new StringBuilder();
        long maxMemory = runtime.maxMemory();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        sb.append("Free memory: " + format.format(freeMemory / 1024) + "KB"
                + CRLF);
        sb.append("Allocated memory: " + format.format(allocatedMemory / 1024)
                + "KB" + CRLF);
        sb.append("Max memory: " + format.format(maxMemory / 1024) + "KB"
                + CRLF);
        sb.append("Total free memory: "
                + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024)
                + "KB" + CRLF);
        LOG.error(sb);

        try {
            MemoryMXBean bean = ManagementFactory.getMemoryMXBean();
            LOG.error(BeanUtils.describe(bean));

            List<MemoryPoolMXBean> poolList = ManagementFactory
                    .getMemoryPoolMXBeans();
            if (poolList != null) {
                for (MemoryPoolMXBean memoryPoolMXBean : poolList) {
                    LOG.error(BeanUtils.describe(memoryPoolMXBean));
                }
            }

            List<GarbageCollectorMXBean> gcList = ManagementFactory
                    .getGarbageCollectorMXBeans();
            if (gcList != null) {
                for (GarbageCollectorMXBean gc : gcList) {
                    LOG.error(BeanUtils.describe(gc));
                }
            }

        } catch (Exception e) {
            LOG.error(e);
        }

    }

    private void dumpDatabaseInfo() {
        LOG.error("PostgreSQL Info:");
        dumpSqlQuery("SELECT * FROM pg_stat_activity order by usename");
        dumpSqlQuery("SELECT * FROM pg_locks pl LEFT JOIN pg_stat_activity psa ON pl.pid = psa.pid");
        dumpSqlQuery("SELECT * FROM pg_locks pl LEFT JOIN pg_prepared_xacts ppx ON pl.virtualtransaction = '-1/' "
                + "|| ppx.transaction");
        dumpSqlQuery("SELECT blocked_locks.pid     AS blocked_pid,          blocked_activity.usename  AS blocked_user,          blocking_locks.pid     AS blocking_pid,          blocking_activity.usename AS blocking_user,          blocked_activity.query    AS blocked_statement,          blocking_activity.query   AS blocking_statement    FROM  pg_catalog.pg_locks         blocked_locks     JOIN pg_catalog.pg_stat_activity blocked_activity  ON blocked_activity.pid = blocked_locks.pid     JOIN pg_catalog.pg_locks         blocking_locks         ON blocking_locks.locktype = blocked_locks.locktype         AND blocking_locks.DATABASE IS NOT DISTINCT FROM blocked_locks.DATABASE         AND blocking_locks.relation IS NOT DISTINCT FROM blocked_locks.relation         AND blocking_locks.page IS NOT DISTINCT FROM blocked_locks.page         AND blocking_locks.tuple IS NOT DISTINCT FROM blocked_locks.tuple         AND blocking_locks.virtualxid IS NOT DISTINCT FROM blocked_locks.virtualxid         AND blocking_locks.transactionid IS NOT DISTINCT FROM blocked_locks.transactionid         AND blocking_locks.classid IS NOT DISTINCT FROM blocked_locks.classid         AND blocking_locks.objid IS NOT DISTINCT FROM blocked_locks.objid         AND blocking_locks.objsubid IS NOT DISTINCT FROM blocked_locks.objsubid         AND blocking_locks.pid != blocked_locks.pid      JOIN pg_catalog.pg_stat_activity blocking_activity ON blocking_activity.pid = blocking_locks.pid    WHERE NOT blocked_locks.granted");
        dumpSqlQuery("SELECT bl.pid                 AS blocked_pid,          a.usename              AS blocked_user,          ka.query               AS blocking_statement,          now() - ka.query_start AS blocking_duration,          kl.pid                 AS blocking_pid,          ka.usename             AS blocking_user,          a.query                AS blocked_statement,          now() - a.query_start  AS blocked_duration   FROM  pg_catalog.pg_locks         bl    JOIN pg_catalog.pg_stat_activity a  ON a.pid = bl.pid    JOIN pg_catalog.pg_locks         kl ON kl.transactionid = bl.transactionid AND kl.pid != bl.pid    JOIN pg_catalog.pg_stat_activity ka ON ka.pid = kl.pid   WHERE NOT bl.granted");
    }

    private void dumpSqlQuery(String sql) {
        LOG.error("Running a query: " + sql);
        StringBuilder sb = new StringBuilder();
        Connection c = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            sb.append(CRLF);
            Properties properties = getDbProperties();
            Class.forName(properties.getProperty("csm.db.driver").trim());
            c = DriverManager.getConnection(
                    properties.getProperty("csm.db.connection.url").trim(),
                    properties.getProperty("csm.db.user").trim(), properties
                            .getProperty("csm.db.password").trim());
            c.setReadOnly(true);

            stmt = c.createStatement();
            stmt.setQueryTimeout(60);

            rs = stmt.executeQuery(sql);
            ResultSetMetaData rsmd = rs.getMetaData();

            int numberOfColumns = rsmd.getColumnCount();
            for (int i = 1; i <= numberOfColumns; i++) {
                if (i > 1)
                    sb.append(",  ");
                String columnName = rsmd.getColumnName(i);
                sb.append(columnName);
            }
            sb.append(CRLF);

            while (rs.next()) {
                for (int i = 1; i <= numberOfColumns; i++) {
                    if (i > 1)
                        sb.append(",  ");
                    String columnValue = rs.getObject(i) + "";
                    sb.append(columnValue);
                }
                sb.append(CRLF);
            }
        } catch (Exception e) {
            LOG.error("Query Failed: " + e);
        } finally {
            DbUtils.closeQuietly(rs);
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(c);
        }
        LOG.error(sb);

    }

    private Properties getDbProperties() {
        Properties properties = new Properties();
        try {
            try {
                properties.load(getClass().getResourceAsStream(
                        "/WEB-INF/classes/csm.properties"));
            } catch (RuntimeException e) {
                properties.load(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("/csm.properties"));
            }
        } catch (Exception e) {
            throw new RuntimeException("ERROR LOADING CSM PROPERTIES FILE!", e);
        }
        return properties;
    }

    private void dumpDiskInfo() {
        LOG.error("Disk Usage:");
        dumpDiskInfo(new File("/"));
        dumpDiskInfo(new File("/local"));

    }

    private void dumpDiskInfo(File file) {
        LOG.error(file.getAbsolutePath() + ": Free Space is "
                + file.getFreeSpace() + "; Usable Space is "
                + file.getUsableSpace() + "; Total Space is "
                + file.getTotalSpace());
    }

    private void dumpRequestProcessingInfo() {
        LOG.error("Request Processing Info:");
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Thread, ServletRequest> entry : RequestTrackingFilter.TRACKER
                .entrySet()) {
            sb.append("Thread " + entry.getKey()
                    + " is processing the following HTTP request:");
            sb.append(CRLF);
            dumpRequestInfo(sb, entry.getValue());
            sb.append(CRLF);
        }
        LOG.error(sb);
    }

    private void dumpRequestInfo(StringBuilder sb, ServletRequest r) {
        HttpServletRequest request = (HttpServletRequest) r;
        sb.append("Context Path: " + request.getContextPath() + CRLF);
        sb.append("Method: " + request.getMethod() + CRLF);
        sb.append("Path Info: " + request.getPathInfo() + CRLF);
        sb.append("Path Translated: " + request.getPathTranslated() + CRLF);
        sb.append("Query String: " + request.getQueryString() + CRLF);
        sb.append("Remote User: " + request.getRemoteUser() + CRLF);
        sb.append("Request URI: " + request.getRequestURI() + CRLF);
        sb.append("Request URL: " + request.getRequestURL() + CRLF);
        sb.append("Servlet Path: " + request.getServletPath() + CRLF);
        sb.append("Remote Addr: " + request.getRemoteAddr() + CRLF);
        sb.append("Session ID: " + request.getSession().getId() + CRLF);
        sb.append("Parameter Map: " + request.getParameterMap() + CRLF);

    }

    @SuppressWarnings("rawtypes")
    private void dumpThreads() {
        LOG.error("Thread Dump:");
        Map<Thread, StackTraceElement[]> allThreads = Thread
                .getAllStackTraces();
        Iterator<Thread> iterator = allThreads.keySet().iterator();
        StringBuffer stringBuffer = new StringBuffer();
        while (iterator.hasNext()) {
            Thread key = iterator.next();
            StackTraceElement[] trace = (StackTraceElement[]) allThreads
                    .get(key);
            stringBuffer.append(key);
            stringBuffer.append(CRLF);
            for (int i = 0; i < trace.length; i++) {
                stringBuffer.append(" " + trace[i] + CRLF); // NOPMD
            }
            stringBuffer.append(CRLF);
        }
        LOG.error(stringBuffer);

        try {
            ThreadMXBean bean = ManagementFactory.getThreadMXBean();
            LOG.error(BeanUtils.describe(bean));

            LOG.error("Thread dump from the ThreadMXBean:");
            LOG.error(CRLF + Arrays.asList(bean.dumpAllThreads(true, true)));

            long[] ids = bean.findDeadlockedThreads();
            dumpThreads(bean, ids);

            ids = bean.findMonitorDeadlockedThreads();
            dumpThreads(bean, ids);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    /**
     * @param bean
     * @param ids
     */
    private void dumpThreads(ThreadMXBean bean, long[] ids) {
        if (ids != null) {
            ThreadInfo[] infos = bean.getThreadInfo(ids, true, true);
            LOG.error("Following Threads are deadlocked:");
            for (ThreadInfo info : infos) {
                LOG.error(info);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        org.apache.log4j.Logger.getRootLogger().addAppender(
                new ConsoleAppender(new SimpleLayout()));
        org.apache.log4j.Logger.getRootLogger().setLevel(
                org.apache.log4j.Level.ERROR);
        new ComprehensiveApplicationStatusLogger().execute();
    }

}

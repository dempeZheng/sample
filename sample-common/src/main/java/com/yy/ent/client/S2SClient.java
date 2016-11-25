package com.yy.ent.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yy.ent.client.codec.S2SDecoder;
import com.yy.ent.client.codec.S2SEncoder;
import com.yy.ent.client.protocol.CreateMetaResult;
import com.yy.ent.client.protocol.MetaFilter;
import com.yy.ent.client.protocol.SnapBeginResult;
import com.yy.ent.client.protocol.TMeta;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;

public class S2SClient extends S2SThriftClient {
    private static final Logger LOGGER = Logger.getLogger(com.yy.ent.client.S2SClient.class);
    public static final String IPS = "ips";
    public static final String IP = "ip";
    public static final String ISP = "isp";
    public static final String PRI_GROUP_ID = "pri_group_id";
    private int groupId = 0;
    private JSONArray ipsArray;

    public S2SClient(String host, int port) {
        super(host, port);
    }

    public S2SClient() {
        JSONObject hostInfoJSON = Conf.readHostInfo(Constants.S2S_HOST_INFO_PATH);
        if(hostInfoJSON.getInteger("pri_group_id") != null) {
            this.groupId = hostInfoJSON.getInteger("pri_group_id").intValue();
        }
        this.ipsArray = hostInfoJSON.getJSONArray("ips");
    }

    public CreateMetaResult createMeta() throws TException, UnknownHostException {
        String cookie = this.authConnect(Constants.S2S_NAME, Constants.S2S_KEY);
        ByteBuffer buffer = this.getLocalHostInfo();
        return this.createMeta(cookie, Constants.S2S_NAME, buffer, this.groupId, Constants.S2S_TYPE);
    }

    public ByteBuffer getLocalHostInfo() throws UnknownHostException {
        S2SEncoder s2SEncoder = new S2SEncoder();
        HashMap map = new HashMap();
        if(this.ipsArray != null) {
            Iterator iterator = this.ipsArray.iterator();

            while(iterator.hasNext()) {
                Object o = iterator.next();
                JSONObject jsonObject = (JSONObject)o;
                int isp = jsonObject.getInteger("isp").intValue();
                if(isp != 10 && isp != 11 && isp != 12 && isp != 20) {
                    String ip = jsonObject.getString("ip");
                    map.put(Integer.valueOf(IntelUtils.getIsp(isp)), Long.valueOf(S2SEncoder.ipToLong(ip)));
                }
            }
        } else {
            LOGGER.warn("hostinfo file not exist!");
            map.put(Integer.valueOf(0), Long.valueOf(S2SEncoder.ipToLong(IntelUtils.getLocalIp())));
        }

        s2SEncoder.writeIpList(map);
        s2SEncoder.writeTcpPort(Constants.S2S_CLIENT_PORT);
        return s2SEncoder.endToByteBuffer();
    }

    public SnapBeginResult getServerByName(String name, int limit) throws TException {
        String cookie = this.authConnect(Constants.S2S_NAME, Constants.S2S_KEY);
        ArrayList filters = new ArrayList();
        MetaFilter filter = new MetaFilter();
        filter.setName(name);
        filters.add(filter);
        return this.snapMetaBegin(cookie, filters, limit);
    }

    public List<SrvInfo> getAvailableServerByName(String name, int limit) throws TException {
        SnapBeginResult result = this.getServerByName(name, limit);
        return this.getSrvInfoByTMetas(result.getMData());
    }

    public List<SrvInfo> getSrvInfoByTMetas(List<TMeta> tMetaList) {
        ArrayList srvInfos = new ArrayList();
        Iterator iterator = tMetaList.iterator();

        TMeta tMeta;
        SrvInfo info;
        while(iterator.hasNext()) {
            tMeta = (TMeta)iterator.next();
            if(this.groupId == tMeta.getGroupId()) {
                info = this.getSrvInfo(tMeta);
                srvInfos.add(info);
            }
        }

        if(srvInfos.size() == 0) {
            iterator = tMetaList.iterator();

            while(iterator.hasNext()) {
                tMeta = (TMeta)iterator.next();
                info = this.getSrvInfo(tMeta);
                srvInfos.add(info);
            }
        }

        Collections.shuffle(srvInfos);
        return srvInfos;
    }

    public SrvInfo getSrvInfo(TMeta tMeta) {
        SrvInfo info = new SrvInfo();
        S2SDecoder s2SDecoder = new S2SDecoder();
        s2SDecoder.decoder(tMeta.getData());
        Map ips = s2SDecoder.getIpList();
        HashMap ipMap = new HashMap();
        Iterator iterator = ips.keySet().iterator();

        while(iterator.hasNext()) {
            Integer key = (Integer)iterator.next();
            ipMap.put(key, S2SDecoder.longToIp(((Long)ips.get(key)).longValue()));
        }

        info.setIpMap(ipMap);
        info.setName(tMeta.getName());
        info.setGroupId(tMeta.getGroupId());
        info.setStatus(tMeta.getStatus());
        info.setServerId(tMeta.getServerId());
        info.setPort(s2SDecoder.getTcpPort());
        return info;
    }


    public static void main(String[] args) throws TException {
        S2SClient s2SClient = new S2SClient();
        List list = s2SClient.getAvailableServerByName("entsrv", 5);
        Iterator iterator = list.iterator();

        while(iterator.hasNext()) {
            SrvInfo info = (SrvInfo) iterator.next();
            System.out.println(info);
        }
    }

}

package com.handheld.uhfr;

import android.util.Log;
import com.BRMicro.SerialPort;
import com.uhf.api.cls.Reader;
import com.uhf.api.cls.Reader.AntPower;
import com.uhf.api.cls.Reader.AntPowerConf;
import com.uhf.api.cls.Reader.EmbededData_ST;
import com.uhf.api.cls.Reader.HardwareDetails;
import com.uhf.api.cls.Reader.HoptableData_ST;
import com.uhf.api.cls.Reader.Inv_Potl;
import com.uhf.api.cls.Reader.Inv_Potls_ST;
import com.uhf.api.cls.Reader.Lock_Obj;
import com.uhf.api.cls.Reader.Lock_Type;
import com.uhf.api.cls.Reader.Mtr_Param;
import com.uhf.api.cls.Reader.READER_ERR;
import com.uhf.api.cls.Reader.Region_Conf;
import com.uhf.api.cls.Reader.SL_TagProtocol;
import com.uhf.api.cls.Reader.TAGINFO;
import com.uhf.api.cls.Reader.TagFilter_ST;
import java.util.ArrayList;
import java.util.List;

public class UHFRManager {
    public static int Port = 12;
    private static Reader reader;
    private int ant = 1;
    private int[] ants = {1};
    private String tag = "UHFRManager";

    public static UHFRManager getIntance() {
        if (connect()) {
            return new UHFRManager();
        }
        return null;
    }

    public boolean close() {
        if (reader != null) {
            reader.CloseReader();
        }
        SerialPort serialPort = new SerialPort();
        serialPort.zigbeepoweroff();
        serialPort.rfidPoweroff();
        reader = null;
        return true;
    }

    public String getHardware() {
        Reader reader2 = reader;
        reader2.getClass();
        HardwareDetails val = new HardwareDetails();
        if (reader.GetHardwareDetails(val) != READER_ERR.MT_OK_ERR) {
            return null;
        }
        String module = val.module.toString();
        if (module.equals("MODOULE_SLR5100") || module.equals("MODOULE_SLR5200") || module.equals("MODOULE_SLR5300")) {
            module = "uhf-l";
        }
        if (module.equals("MODOULE_SLR1200") || module.equals("MODOULE_SLR1100") || module.equals("MODOULE_SLR1300")) {
            module = "uhf-r";
        }
        if (module.equals("MODOULE_M6E_MICRO")) {
            return "uhf-m";
        }
        return module;
    }

    private static boolean connect() {
        reader = new Reader();
        SerialPort serialPort = new SerialPort();
        serialPort.zigbee_poweron();
        serialPort.rfidPoweron();
        serialPort.switch2channel(Port);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (reader.InitReader_Notype("/dev/ttyMT1", 1) != READER_ERR.MT_OK_ERR) {
            return false;
        }
        connect2();
        return true;
    }

    private static boolean connect2() {
        Reader reader2 = reader;
        reader2.getClass();
        Inv_Potls_ST ipst = new Inv_Potls_ST();
        List<SL_TagProtocol> ltp = new ArrayList<>();
        ltp.add(SL_TagProtocol.SL_TAG_PROTOCOL_GEN2);
        ipst.potlcnt = ltp.size();
        ipst.potls = new Inv_Potl[ipst.potlcnt];
        SL_TagProtocol[] stp = (SL_TagProtocol[]) ltp.toArray(new SL_TagProtocol[ipst.potlcnt]);
        for (int i = 0; i < ipst.potlcnt; i++) {
            Reader reader3 = reader;
            reader3.getClass();
            Inv_Potl ipl = new Inv_Potl();
            ipl.weight = 30;
            ipl.potl = stp[i];
            ipst.potls[0] = ipl;
        }
        if (reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_INVPOTL, ipst) == READER_ERR.MT_OK_ERR) {
            return true;
        }
        return false;
    }

    public READER_ERR asyncStartReading() {
        return reader.AsyncStartReading(this.ants, 1, 0);
    }

    public READER_ERR asyncStopReading() {
        return reader.AsyncStopReading();
    }

    public boolean setInventoryFilter(byte[] fdata, int fbank, int fstartaddr, boolean matching) {
        Reader reader2 = reader;
        reader2.getClass();
        TagFilter_ST g2tf = new TagFilter_ST();
        g2tf.fdata = fdata;
        g2tf.flen = fdata.length * 8;
        if (matching) {
            g2tf.isInvert = 0;
        } else {
            g2tf.isInvert = 1;
        }
        g2tf.bank = fbank;
        g2tf.startaddr = fstartaddr * 16;
        READER_ERR er = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, g2tf);
        if (er == READER_ERR.MT_OK_ERR) {
            return true;
        }
        Log.e(this.tag, er.toString());
        return false;
    }

    public boolean setCancleInventoryFilter() {
        READER_ERR er = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, null);
        if (er == READER_ERR.MT_OK_ERR) {
            return true;
        }
        Log.e(this.tag, er.toString());
        return false;
    }

    public List<TAGINFO> tagInventoryRealTime() {
        List<TAGINFO> list = new ArrayList<>();
        int[] tagcnt = new int[1];
        READER_ERR AsyncGetTagCount = reader.AsyncGetTagCount(tagcnt);
        for (int i = 0; i < tagcnt[0]; i++) {
            Reader reader2 = reader;
            reader2.getClass();
            TAGINFO tfs = new TAGINFO();
            if (reader.AsyncGetNextTag(tfs) == READER_ERR.MT_OK_ERR) {
                list.add(tfs);
            }
        }
        if (list.size() >= 0) {
            return list;
        }
        return null;
    }

    public boolean stopTagInventory() {
        READER_ERR er = reader.AsyncStopReading();
        if (er == READER_ERR.MT_OK_ERR) {
            return true;
        }
        Log.e(this.tag, er.toString());
        return false;
    }

    public List<TAGINFO> tagInventoryByTimer(short readtime) {
        List<TAGINFO> list = new ArrayList<>();
        int[] tagcnt = new int[1];
        READER_ERR TagInventory_Raw = reader.TagInventory_Raw(this.ants, 1, readtime, tagcnt);
        for (int i = 0; i < tagcnt[0]; i++) {
            Reader reader2 = reader;
            reader2.getClass();
            TAGINFO tfs = new TAGINFO();
            if (reader.GetNextTag(tfs) == READER_ERR.MT_OK_ERR) {
                list.add(tfs);
            }
        }
        if (list.size() >= 0) {
            return list;
        }
        return null;
    }

    public List<TAGINFO> tagEpcTidInventoryByTimer(short readtime) {
        List<TAGINFO> list = new ArrayList<>();
        Reader reader2 = reader;
        reader2.getClass();
        EmbededData_ST edst = new EmbededData_ST();
        edst.bank = 2;
        edst.startaddr = 0;
        edst.bytecnt = 12;
        edst.accesspwd = null;
        READER_ERR ParamSet = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_EMBEDEDDATA, edst);
        int[] tagcnt = new int[1];
        READER_ERR er = reader.TagInventory_Raw(this.ants, 1, readtime, tagcnt);
        for (int i = 0; i < tagcnt[0]; i++) {
            Reader reader3 = reader;
            reader3.getClass();
            TAGINFO tfs = new TAGINFO();
            if (reader.GetNextTag(tfs) == READER_ERR.MT_OK_ERR) {
                list.add(tfs);
            }
        }
        if (list.size() >= 0) {
            return list;
        }
        return null;
    }

    public List<TAGINFO> tagEpcOtherInventoryByTimer(short readtime, int bank, int startaddr, int bytecnt, byte[] accesspwd) {
        List<TAGINFO> list = new ArrayList<>();
        Reader reader2 = reader;
        reader2.getClass();
        EmbededData_ST edst = new EmbededData_ST();
        edst.bank = bank;
        edst.startaddr = startaddr;
        edst.bytecnt = bytecnt;
        edst.accesspwd = accesspwd;
        READER_ERR ParamSet = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_EMBEDEDDATA, edst);
        int[] tagcnt = new int[1];
        READER_ERR er = reader.TagInventory_Raw(this.ants, 1, readtime, tagcnt);
        for (int i = 0; i < tagcnt[0]; i++) {
            Reader reader3 = reader;
            reader3.getClass();
            TAGINFO tfs = new TAGINFO();
            if (reader.GetNextTag(tfs) == READER_ERR.MT_OK_ERR) {
                list.add(tfs);
            }
        }
        if (list.size() >= 0) {
            return list;
        }
        return null;
    }

    public READER_ERR getTagData(int mbank, int startaddr, int len, byte[] rdata, byte[] password, short timeout) {
        READER_ERR er;
        READER_ERR er2 = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, null);
        if (er2 == READER_ERR.MT_OK_ERR) {
            int trycount = 3;
            do {
                er = reader.GetTagData(this.ant, (char) mbank, startaddr, len, rdata, password, timeout);
                trycount--;
                if (trycount < 1) {
                    break;
                }
            } while (er != READER_ERR.MT_OK_ERR);
            if (er == READER_ERR.MT_OK_ERR) {
                return er;
            }
            Log.e(this.tag, er.toString());
            return er;
        }
        Log.e(this.tag, er2.toString());
        return er2;
    }

    public byte[] getTagDataByFilter(int mbank, int startaddr, int len, byte[] password, short timeout, byte[] fdata, int fbank, int fstartaddr, boolean matching) {
        Reader reader2 = reader;
        reader2.getClass();
        TagFilter_ST g2tf = new TagFilter_ST();
        g2tf.fdata = fdata;
        g2tf.flen = fdata.length * 8;
        if (matching) {
            g2tf.isInvert = 0;
        } else {
            g2tf.isInvert = 1;
        }
        g2tf.bank = fbank;
        g2tf.startaddr = fstartaddr * 16;
        byte[] rdata = new byte[(len * 2)];
        READER_ERR er = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, g2tf);
        if (er == READER_ERR.MT_OK_ERR) {
            READER_ERR er2 = reader.GetTagData(this.ant, (char) mbank, startaddr, len, rdata, password, timeout);
            if (er2 == READER_ERR.MT_OK_ERR) {
                return rdata;
            }
            Log.e("read by filter read", er2.toString());
            return null;
        }
        Log.e("read by filter set", er.toString());
        return null;
    }

    public READER_ERR writeTagData(char mbank, int startaddress, byte[] data, int datalen, byte[] accesspasswd, short timeout) {
        READER_ERR er;
        READER_ERR er2 = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, null);
        if (er2 == READER_ERR.MT_OK_ERR) {
            int trycount = 3;
            do {
                er = reader.WriteTagData(1, mbank, startaddress, data, datalen, accesspasswd, timeout);
                trycount--;
                if (trycount < 1) {
                    break;
                }
            } while (er != READER_ERR.MT_OK_ERR);
            if (er == READER_ERR.MT_OK_ERR) {
                return er;
            }
            Log.e(this.tag, er.toString());
            return er;
        }
        Log.e(this.tag, er2.toString());
        return er2;
    }

    public READER_ERR writeTagDataByFilter(char mbank, int startaddress, byte[] data, int datalen, byte[] accesspasswd, short timeout, byte[] fdata, int fbank, int fstartaddr, boolean matching) {
        READER_ERR er;
        Reader reader2 = reader;
        reader2.getClass();
        TagFilter_ST g2tf = new TagFilter_ST();
        g2tf.fdata = fdata;
        g2tf.flen = fdata.length * 8;
        if (matching) {
            g2tf.isInvert = 0;
        } else {
            g2tf.isInvert = 1;
        }
        g2tf.bank = fbank;
        g2tf.startaddr = fstartaddr * 16;
        READER_ERR er2 = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, g2tf);
        if (er2 == READER_ERR.MT_OK_ERR) {
            int trycount = 3;
            do {
                er = reader.WriteTagData(1, mbank, startaddress, data, datalen, accesspasswd, timeout);
                trycount--;
                if (trycount < 1) {
                    break;
                }
            } while (er != READER_ERR.MT_OK_ERR);
            if (er == READER_ERR.MT_OK_ERR) {
                return er;
            }
            Log.e(this.tag, er.toString());
            return er;
        }
        Log.e(this.tag, er2.toString());
        return er2;
    }

    public READER_ERR writeTagEPC(byte[] data, byte[] accesspwd, short timeout) {
        READER_ERR er;
        READER_ERR ParamSet = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, null);
        int trycount = 3;
        do {
            er = reader.WriteTagEpcEx(this.ant, data, data.length, accesspwd, timeout);
            trycount--;
            if (trycount < 1) {
                break;
            }
        } while (er != READER_ERR.MT_OK_ERR);
        if (er != READER_ERR.MT_OK_ERR) {
            Log.e(this.tag, er.toString());
        }
        return er;
    }

    public READER_ERR writeTagEPCByFilter(byte[] data, byte[] accesspwd, short timeout, byte[] fdata, int fbank, int fstartaddr, boolean matching) {
        READER_ERR er;
        Reader reader2 = reader;
        reader2.getClass();
        TagFilter_ST g2tf = new TagFilter_ST();
        g2tf.fdata = fdata;
        g2tf.flen = fdata.length * 8;
        if (matching) {
            g2tf.isInvert = 0;
        } else {
            g2tf.isInvert = 1;
        }
        g2tf.bank = fbank;
        g2tf.startaddr = fstartaddr * 16;
        READER_ERR er2 = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, g2tf);
        if (er2 == READER_ERR.MT_OK_ERR) {
            do {
                er = reader.WriteTagEpcEx(this.ant, data, data.length, accesspwd, timeout);
                if (3 < 1) {
                    break;
                }
            } while (er != READER_ERR.MT_OK_ERR);
            if (er == READER_ERR.MT_OK_ERR) {
                return er;
            }
            Log.e(this.tag, er.toString());
            return er;
        }
        Log.e(this.tag, er2.toString());
        return er2;
    }

    public READER_ERR lockTag(Lock_Obj lockobject, Lock_Type locktype, byte[] accesspasswd, short timeout) {
        READER_ERR er = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, null);
        if (er == READER_ERR.MT_OK_ERR) {
            er = reader.LockTag(this.ant, (byte) lockobject.value(), (short) locktype.value(), accesspasswd, timeout);
        }
        if (er != READER_ERR.MT_OK_ERR) {
            Log.e(this.tag, er.toString());
        }
        return er;
    }

    public READER_ERR lockTagByFilter(Lock_Obj lockobject, Lock_Type locktype, byte[] accesspasswd, short timeout, byte[] fdata, int fbank, int fstartaddr, boolean matching) {
        Reader reader2 = reader;
        reader2.getClass();
        TagFilter_ST g2tf = new TagFilter_ST();
        g2tf.fdata = fdata;
        g2tf.flen = fdata.length * 8;
        if (matching) {
            g2tf.isInvert = 0;
        } else {
            g2tf.isInvert = 1;
        }
        g2tf.bank = fbank;
        g2tf.startaddr = fstartaddr * 16;
        READER_ERR er = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, g2tf);
        if (er == READER_ERR.MT_OK_ERR) {
            er = reader.LockTag(this.ant, (byte) lockobject.value(), (short) locktype.value(), accesspasswd, timeout);
        }
        if (er != READER_ERR.MT_OK_ERR) {
            Log.e(this.tag, er.toString());
        }
        return er;
    }

    public READER_ERR killTag(byte[] killpasswd, short timeout) {
        READER_ERR er = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, null);
        if (er == READER_ERR.MT_OK_ERR) {
            er = reader.KillTag(this.ant, killpasswd, timeout);
        }
        if (er != READER_ERR.MT_OK_ERR) {
            Log.e(this.tag, er.toString());
        }
        return er;
    }

    public READER_ERR killTagByFilter(byte[] killpasswd, short timeout, byte[] fdata, int fbank, int fstartaddr, boolean matching) {
        Reader reader2 = reader;
        reader2.getClass();
        TagFilter_ST g2tf = new TagFilter_ST();
        g2tf.fdata = fdata;
        g2tf.flen = fdata.length * 8;
        if (matching) {
            g2tf.isInvert = 0;
        } else {
            g2tf.isInvert = 1;
        }
        g2tf.bank = fbank;
        g2tf.startaddr = fstartaddr * 16;
        READER_ERR er = reader.ParamSet(Mtr_Param.MTR_PARAM_TAG_FILTER, g2tf);
        if (er == READER_ERR.MT_OK_ERR) {
            er = reader.KillTag(this.ant, killpasswd, timeout);
        }
        if (er != READER_ERR.MT_OK_ERR) {
            Log.e(this.tag, er.toString());
        }
        return er;
    }

    public READER_ERR setRegion(Region_Conf region) {
        return reader.ParamSet(Mtr_Param.MTR_PARAM_FREQUENCY_REGION, region);
    }

    public Region_Conf getRegion() {
        Region_Conf[] rcf2 = new Region_Conf[1];
        READER_ERR er = reader.ParamGet(Mtr_Param.MTR_PARAM_FREQUENCY_REGION, rcf2);
        if (er == READER_ERR.MT_OK_ERR) {
            return rcf2[0];
        }
        Log.e(this.tag, er.toString());
        return null;
    }

    public int[] getFrequencyPoints() {
        Reader reader2 = reader;
        reader2.getClass();
        HoptableData_ST hdst2 = new HoptableData_ST();
        READER_ERR er = reader.ParamGet(Mtr_Param.MTR_PARAM_FREQUENCY_HOPTABLE, hdst2);
        if (er == READER_ERR.MT_OK_ERR) {
            return Sort(hdst2.htb, hdst2.lenhtb);
        }
        Log.e(this.tag, er.toString());
        return null;
    }

    public READER_ERR setFrequencyPoints(int[] frequencyPoints) {
        Reader reader2 = reader;
        reader2.getClass();
        HoptableData_ST hdst = new HoptableData_ST();
        hdst.lenhtb = frequencyPoints.length;
        hdst.htb = frequencyPoints;
        return reader.ParamSet(Mtr_Param.MTR_PARAM_FREQUENCY_HOPTABLE, hdst);
    }

    public READER_ERR setPower(int readPower, int writePower) {
        Reader reader2 = reader;
        reader2.getClass();
        AntPowerConf antPowerConf = new AntPowerConf();
        antPowerConf.antcnt = this.ant;
        Reader reader3 = reader;
        reader3.getClass();
        AntPower antPower = new AntPower();
        antPower.antid = 1;
        antPower.readPower = (short) (((short) readPower) * 100);
        antPower.writePower = (short) (((short) writePower) * 100);
        antPowerConf.Powers[0] = antPower;
        return reader.ParamSet(Mtr_Param.MTR_PARAM_RF_ANTPOWER, antPowerConf);
    }

    public int[] getPower() {
        int[] powers = new int[2];
        Reader reader2 = reader;
        reader2.getClass();
        AntPowerConf apcf2 = new AntPowerConf();
        READER_ERR er = reader.ParamGet(Mtr_Param.MTR_PARAM_RF_ANTPOWER, apcf2);
        if (er == READER_ERR.MT_OK_ERR) {
            powers[0] = apcf2.Powers[0].readPower / 100;
            powers[1] = apcf2.Powers[0].writePower / 100;
            return powers;
        }
        Log.e(this.tag, er.toString());
        return null;
    }

    public int getTemperature() {
        int[] val = {0};
        READER_ERR er = reader.ParamGet(Mtr_Param.MTR_PARAM_RF_TEMPERATURE, val);
        if (er == READER_ERR.MT_OK_ERR) {
            return val[0];
        }
        Log.e(this.tag, er.toString());
        return -1;
    }

    public READER_ERR setFastMode() {
        READER_ERR er = setPower(30, 30);
        if (er != READER_ERR.MT_OK_ERR) {
            return er;
        }
        return reader.ParamSet(Mtr_Param.MTR_PARAM_POTL_GEN2_SESSION, new int[]{1});
    }

    public READER_ERR setCancleFastMode() {
        return reader.ParamSet(Mtr_Param.MTR_PARAM_POTL_GEN2_SESSION, new int[1]);
    }

    private int[] Sort(int[] array, int len) {
        for (int xIndex = 0; xIndex < len; xIndex++) {
            for (int yIndex = 0; yIndex < len; yIndex++) {
                if (array[xIndex] < array[yIndex]) {
                    int tmpIntValue = array[xIndex];
                    array[xIndex] = array[yIndex];
                    array[yIndex] = tmpIntValue;
                }
            }
        }
        return array;
    }
}

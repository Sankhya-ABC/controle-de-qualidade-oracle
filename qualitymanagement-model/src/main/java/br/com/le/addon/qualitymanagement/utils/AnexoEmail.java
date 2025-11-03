package br.com.le.addon.qualitymanagement.utils;

public class AnexoEmail {
    private byte[] data;

    private String fileName;

    private String mimeType;

    public AnexoEmail(String fileName, String mimeType, byte[] data) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.data = data;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return this.mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}

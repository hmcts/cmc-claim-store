package uk.gov.hmcts.document.model;

import org.springframework.http.HttpStatus;

public class FileUploadResponse {

    private final HttpStatus status;
    private final String fileUrl;
    private final String fileName;
    private final String mimeType;
    private final String createdBy;
    private final String lastModifiedBy;
    private final String createdOn;
    private final String modifiedOn;


    public FileUploadResponse(HttpStatus status) {
        this(status, null, null, null,
            null, null, null, null);
    }

    public FileUploadResponse(HttpStatus status, String fileUrl, String fileName,
                              String mimeType, String createdBy, String createdOn,
                              String lastModifiedBy, String modifiedOn) {
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.createdBy = createdBy;
        this.lastModifiedBy = lastModifiedBy;
        this.createdOn = createdOn;
        this.modifiedOn = modifiedOn;
        this.status = status;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public String getModifiedOn() {
        return modifiedOn;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

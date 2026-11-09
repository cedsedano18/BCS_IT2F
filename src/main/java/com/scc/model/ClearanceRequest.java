
package com.scc.model;

public class ClearanceRequest {

    private int requestId;
    private String residentName;
    private String address;
    private String purpose;
    private String status;
    private int encodedBy;
    private String createdAt;
    private String updatedAt;

    public ClearanceRequest(
            int requestId,
            String residentName,
            String address,
            String purpose,
            String status,
            int encodedBy,
            String createdAt,
            String updatedAt) {

        this.requestId = requestId;
        this.residentName = residentName;
        this.address = address;
        this.purpose = purpose;
        this.status = status;
        this.encodedBy = encodedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getResidentName() {
        return residentName;
    }

    public String getAddress() {
        return address;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getStatus() {
        return status;
    }

    public int getEncodedBy() {
        return encodedBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}

package com.smartblood.model;

public class Request {
    private int id;
    private int requesterUserId;
    private String requesterName;
    private String patientName;
    private String bloodGroup;
    private String location;
    private String city;
    private String reason;
    private int unitsRequired;
    private boolean emergency;
    private String status;
    private String matchedDonorSummary;
    private String createdAt;
    private int selectedDonorUserId;
    private int assignedDonorUserId;
    private String assignedDonorName;
    private String assignedDonorEmail;
    private String assignedDonorPhone;
    private String contactName;
    private String contactPhone;
    private String hospitalName;
    private String requiredBy;
    private String formalityNotes;
    private String procurementStatus;
    private String completedAt;
    private String donorResponseStatus;
    private String donorResponseNote;
    private String certificateNumber;
    private boolean targetedRequest;
    private boolean certificateAvailable;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRequesterUserId() {
        return requesterUserId;
    }

    public void setRequesterUserId(int requesterUserId) {
        this.requesterUserId = requesterUserId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getUnitsRequired() {
        return unitsRequired;
    }

    public void setUnitsRequired(int unitsRequired) {
        this.unitsRequired = unitsRequired;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMatchedDonorSummary() {
        return matchedDonorSummary;
    }

    public void setMatchedDonorSummary(String matchedDonorSummary) {
        this.matchedDonorSummary = matchedDonorSummary;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getSelectedDonorUserId() {
        return selectedDonorUserId;
    }

    public void setSelectedDonorUserId(int selectedDonorUserId) {
        this.selectedDonorUserId = selectedDonorUserId;
    }

    public int getAssignedDonorUserId() {
        return assignedDonorUserId;
    }

    public void setAssignedDonorUserId(int assignedDonorUserId) {
        this.assignedDonorUserId = assignedDonorUserId;
    }

    public String getAssignedDonorName() {
        return assignedDonorName;
    }

    public void setAssignedDonorName(String assignedDonorName) {
        this.assignedDonorName = assignedDonorName;
    }

    public String getAssignedDonorEmail() {
        return assignedDonorEmail;
    }

    public void setAssignedDonorEmail(String assignedDonorEmail) {
        this.assignedDonorEmail = assignedDonorEmail;
    }

    public String getAssignedDonorPhone() {
        return assignedDonorPhone;
    }

    public void setAssignedDonorPhone(String assignedDonorPhone) {
        this.assignedDonorPhone = assignedDonorPhone;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }

    public String getRequiredBy() {
        return requiredBy;
    }

    public void setRequiredBy(String requiredBy) {
        this.requiredBy = requiredBy;
    }

    public String getFormalityNotes() {
        return formalityNotes;
    }

    public void setFormalityNotes(String formalityNotes) {
        this.formalityNotes = formalityNotes;
    }

    public String getProcurementStatus() {
        return procurementStatus;
    }

    public void setProcurementStatus(String procurementStatus) {
        this.procurementStatus = procurementStatus;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public String getDonorResponseStatus() {
        return donorResponseStatus;
    }

    public void setDonorResponseStatus(String donorResponseStatus) {
        this.donorResponseStatus = donorResponseStatus;
    }

    public String getDonorResponseNote() {
        return donorResponseNote;
    }

    public void setDonorResponseNote(String donorResponseNote) {
        this.donorResponseNote = donorResponseNote;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public boolean isTargetedRequest() {
        return targetedRequest;
    }

    public void setTargetedRequest(boolean targetedRequest) {
        this.targetedRequest = targetedRequest;
    }

    public boolean isCertificateAvailable() {
        return certificateAvailable;
    }

    public void setCertificateAvailable(boolean certificateAvailable) {
        this.certificateAvailable = certificateAvailable;
    }
}

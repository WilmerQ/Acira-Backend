/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.clases;

/**
 *
 * @author wilme
 */
public class resultadoFirebaseFCM {

    private Long multicast_id;
    private Long success;
    private Long failure;
    private Long canonical_ids;

    public Long getMulticast_id() {
        return multicast_id;
    }

    public void setMulticast_id(Long multicast_id) {
        this.multicast_id = multicast_id;
    }

    public Long getSuccess() {
        return success;
    }

    public void setSuccess(Long success) {
        this.success = success;
    }

    public Long getFailure() {
        return failure;
    }

    public void setFailure(Long failure) {
        this.failure = failure;
    }

    public Long getCanonical_ids() {
        return canonical_ids;
    }

    public void setCanonical_ids(Long canonical_ids) {
        this.canonical_ids = canonical_ids;
    }

}

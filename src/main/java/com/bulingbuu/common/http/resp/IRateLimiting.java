package com.bulingbuu.common.http.resp;

public interface IRateLimiting {

    public int getRateLimitQuota();

    public int getRateLimitRemaining();

    public int getRateLimitReset();

}

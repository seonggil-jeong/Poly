package kopo.poly.persistance.redis;

import kopo.poly.dto.RedisDTO;

public interface IMyRedisMapper {
    int saveRedisString(String redisKey, RedisDTO pDTO) throws Exception;

    RedisDTO getRedisString(String redisKey) throws Exception;

    int saveRedisStringJSON(String redisKey, RedisDTO pDTO) throws Exception;
}

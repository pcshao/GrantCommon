package cn.pcshao.graduaction.service.impl;

import cn.pcshao.graduaction.service.UserService;
import cn.pcshao.grant.common.base.BaseDao;
import cn.pcshao.grant.common.base.BaseServiceImpl;
import cn.pcshao.grant.common.consts.DtoCodeConsts;
import cn.pcshao.grant.common.dao.GrantRoleMapper;
import cn.pcshao.grant.common.dao.GrantUserMapper;
import cn.pcshao.grant.common.dao.GrantUserRoleMapper;
import cn.pcshao.grant.common.entity.*;
import cn.pcshao.grant.common.exception.CustomException;
import cn.pcshao.grant.common.util.ListUtils;
import cn.pcshao.grant.common.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service("userServiceImpl")
public class UserServiceImpl extends BaseServiceImpl<GrantUser, Long> implements UserService {

    @Resource
    private GrantUserMapper grantUserMapper;

    @Resource
    private GrantRoleMapper grantRoleMapper;

    @Resource
    private GrantUserRoleMapper grantUserRoleMapper;

    @Override
    public BaseDao getDao() {
        return grantUserMapper;
    }

    /**
     * 验证登录
     * @param username
     * @param password
     * @return
     */
    public GrantUser doAuth(String username, String password){
        GrantUserExample grantUserExample = new GrantUserExample();
        grantUserExample.createCriteria().andUsernameEqualTo(username).andPasswordEqualTo(password);
        return grantUserMapper.selectByExample(grantUserExample).get(0);
    }

    @Override
    public List<GrantUser> listUsersByUserName(String username) {
        GrantUserExample grantUserExample = new GrantUserExample();
        grantUserExample.createCriteria().andUsernameEqualTo(username);
        return grantUserMapper.selectByExample(grantUserExample);
    }

    @Override
    public List<GrantUser> listUserByRoleId(Short roleId) {
        return grantUserMapper.selectUsersByRoleId(roleId);
    }

    @Override
    public void saveUser(GrantUser grantUser, List<Short> roleIdList) {
        grantUser.setIsUse(true);
        try {
            grantUserMapper.insertSelective(grantUser);
        }catch (Exception e){
            throw new CustomException(DtoCodeConsts.DB_PRIMARY_EXIST, DtoCodeConsts.DB_PRIMARY_EXIST_MSG);
        }
        //默认角色2 normal
        if(ListUtils.isEmptyList(roleIdList)){
            roleIdList = new ArrayList<>();
            roleIdList.add((short)2);
        }
        //@TODO 查出新增记录的自增ID优化
        GrantUserExample grantUserExample = new GrantUserExample();
        grantUserExample.createCriteria().andUsernameEqualTo(grantUser.getUsername());
        Long userId = grantUserMapper.selectByExample(grantUserExample).get(0).getUserId();
        this.bindUserRoles(userId, roleIdList);
    }

    @Override
    public void bindUserRoles(Long userId, List<Short> roleIdList) {
        GrantUserRoleExample example = new GrantUserRoleExample();
        GrantUserRoleExample.Criteria criteria = example.createCriteria();
        criteria.andUserIdEqualTo(userId);
        grantUserRoleMapper.deleteByExample(example);
        for(Short id : roleIdList){
            GrantRole grantRole = grantRoleMapper.selectByPrimaryKey(id);
            GrantUserRole grantUserRole = new GrantUserRole();
            grantUserRole.setUserId(userId);
            grantUserRole.setRoleId(id);
            grantUserRole.setRoleName(grantRole.getRoleName());
            grantUserRoleMapper.insert(grantUserRole);
        }
    }

    @Override
    public List<GrantUser> listUsers(GrantUser grantUser, String roleName, String permissionName) {
        GrantUserExample example = new GrantUserExample();
        GrantUserExample.Criteria criteria = example.createCriteria();
        if(null != grantUser) {
            if (StringUtils.isNotEmpty(grantUser.getUsername())) {
                criteria.andUsernameLike(grantUser.getUsername());
            }
            if (StringUtils.isNotEmpty(grantUser.getNickname())) {
                criteria.andNicknameLike(grantUser.getNickname());
            }
            if (StringUtils.isNotEmpty(grantUser.getEmail())) {
                criteria.andEmailLike(grantUser.getEmail());
            }
            if (StringUtils.isNotEmpty(grantUser.getTel())) {
                criteria.andTelLike(grantUser.getTel());
            }
            if (null != grantUser.getSex()) {
                criteria.andSexEqualTo(grantUser.getSex());
            }
            if (null != grantUser.getIsUse()) {
                criteria.andIsUseEqualTo(grantUser.getIsUse());
            }
            if (null != grantUser.getUserId()) {
                criteria.andUserIdEqualTo(grantUser.getUserId());
            }
        }
        if (null != roleName) {
            //TODO 条件查询用户列表用
        }
        if (null != permissionName) {
            //TODO 条件查询用户列表用
        }
        return grantUserMapper.selectByExample(example);
    }

}

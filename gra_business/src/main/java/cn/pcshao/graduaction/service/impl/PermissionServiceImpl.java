package cn.pcshao.graduaction.service.impl;

import cn.pcshao.graduaction.service.PermissionService;
import cn.pcshao.grant.common.base.BaseDao;
import cn.pcshao.grant.common.base.BaseServiceImpl;
import cn.pcshao.grant.common.consts.DtoCodeConsts;
import cn.pcshao.grant.common.dao.GrantPermissionMapper;
import cn.pcshao.grant.common.dao.GrantRolePermissionMapper;
import cn.pcshao.grant.common.entity.GrantPermission;
import cn.pcshao.grant.common.entity.GrantPermissionExample;
import cn.pcshao.grant.common.entity.GrantRolePermission;
import cn.pcshao.grant.common.exception.CustomException;
import cn.pcshao.grant.common.util.ListUtils;
import cn.pcshao.grant.common.util.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service("permissionServiceImpl")
public class PermissionServiceImpl extends BaseServiceImpl<GrantPermission, Long> implements PermissionService {

    @Resource
    private GrantPermissionMapper grantPermissionMapper;

    @Resource
    private GrantRolePermissionMapper grantRolePermissionMapper;

    @Override
    public void savePermission(GrantPermission grantPermission, List<Short> roleIdList) {
        try{
            grantPermissionMapper.insert(grantPermission);
        }catch (Exception e){
            //2019-05-06改造角色、权限新增时的自增ID返回，发现这里在控制层已经做过处理，这里的try可以去掉
            throw new CustomException(DtoCodeConsts.DB_PRIMARY_EXIST, DtoCodeConsts.DB_PRIMARY_EXIST_MSG);
        }
        if(ListUtils.isEmptyList(roleIdList))
            return;
        this.bindPermissionRoles(grantPermission.getPermissionId(), roleIdList);
    }

    @Override
    public void bindPermissionRoles(Long permissionId, List<Short> roleIdList) {
        for(Short s : roleIdList){
            GrantPermission grantPermission = grantPermissionMapper.selectByPrimaryKey(permissionId);
            GrantRolePermission grantRolePermission = new GrantRolePermission();
            grantRolePermission.setPermissionId(permissionId);
            grantRolePermission.setRoleId(s);
            grantRolePermission.setPermissionName(grantPermission.getPermissionName());
            grantRolePermissionMapper.insertSelective(grantRolePermission);
        }
    }

    @Override
    public boolean findPermissionByName(String permissionName) {
        GrantPermissionExample grantPermissionExample = new GrantPermissionExample();
        grantPermissionExample.createCriteria().andPermissionNameEqualTo(permissionName);
        return grantPermissionMapper.countByExample(grantPermissionExample) > 0;
    }

    @Override
    public List<GrantPermission> listPermissions(GrantPermission grantPermission) {
        GrantPermissionExample grantPermissionExample = new GrantPermissionExample();
        GrantPermissionExample.Criteria criteria = grantPermissionExample.createCriteria();
        if(null != grantPermission) {
            if (StringUtils.isNotEmpty(grantPermission.getPermissionName())) {
                criteria.andPermissionNameLike(grantPermission.getPermissionName());
            }
            if (null != grantPermission.getPermissionId()) {
                criteria.andPermissionIdEqualTo(grantPermission.getPermissionId());
            }
        }
        return grantPermissionMapper.selectByExample(grantPermissionExample);
    }

    @Override
    public List<GrantPermission> listPermissionsByRoleId(Short roleId){
        return grantPermissionMapper.selectPermissionsByRoleId(roleId);
    }

    @Override
    public BaseDao<GrantPermission, Long> getDao() {
        return grantPermissionMapper;
    }
}

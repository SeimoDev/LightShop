package com.lightshop.servlet;

import com.google.gson.JsonObject;
import com.lightshop.dao.AddressDao;
import com.lightshop.model.Address;
import com.lightshop.util.JsonUtil;
import com.lightshop.util.StringUtil;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class AddressServlet extends HttpServlet {
    private final AddressDao addressDao = new AddressDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                JsonUtil.writeError(response, 401, "未登录");
                return;
            }

            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                List<Address> addresses = addressDao.findByUserId(userId);
                JsonUtil.writeSuccess(response, addresses);
            } else if (pathInfo.equals("/default")) {
                Address address = addressDao.findDefaultByUserId(userId);
                JsonUtil.writeSuccess(response, address);
            } else {
                int addressId = Integer.parseInt(pathInfo.substring(1));
                Address address = addressDao.findById(addressId);
                if (address == null || address.getUserId() != userId) {
                    JsonUtil.writeError(response, 404, "地址不存在");
                    return;
                }
                JsonUtil.writeSuccess(response, address);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                JsonUtil.writeError(response, 401, "未登录");
                return;
            }

            String body = JsonUtil.readRequestBody(request);
            JsonObject json = JsonUtil.parseJson(body);

            String receiverName = json.has("receiverName") ? json.get("receiverName").getAsString() : null;
            String phone = json.has("phone") ? json.get("phone").getAsString() : null;
            String province = json.has("province") ? json.get("province").getAsString() : "";
            String city = json.has("city") ? json.get("city").getAsString() : "";
            String district = json.has("district") ? json.get("district").getAsString() : "";
            String detailAddress = json.has("detailAddress") ? json.get("detailAddress").getAsString() : null;
            boolean isDefault = json.has("isDefault") && json.get("isDefault").getAsBoolean();

            // Validation
            if (StringUtil.isEmpty(receiverName)) {
                JsonUtil.writeError(response, 400, "收货人姓名不能为空");
                return;
            }
            if (StringUtil.isEmpty(phone)) {
                JsonUtil.writeError(response, 400, "手机号不能为空");
                return;
            }
            if (StringUtil.isEmpty(detailAddress)) {
                JsonUtil.writeError(response, 400, "详细地址不能为空");
                return;
            }

            Address address = new Address(userId, receiverName, phone, province, city, district, detailAddress);
            address.setDefault(isDefault);

            if (isDefault) {
                addressDao.clearDefault(userId);
            }

            int addressId = addressDao.create(address);
            if (addressId < 0) {
                JsonUtil.writeError(response, 500, "添加地址失败");
                return;
            }

            address.setId(addressId);
            JsonUtil.writeSuccess(response, "添加成功", address);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                JsonUtil.writeError(response, 401, "未登录");
                return;
            }

            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonUtil.writeError(response, 400, "地址ID不能为空");
                return;
            }

            // Check if setting default
            if (pathInfo.endsWith("/default")) {
                int addressId = Integer.parseInt(pathInfo.replace("/default", "").substring(1));
                addressDao.setDefault(addressId, userId);
                JsonUtil.writeSuccess(response, "设置成功", null);
                return;
            }

            int addressId = Integer.parseInt(pathInfo.substring(1));
            Address address = addressDao.findById(addressId);
            if (address == null || address.getUserId() != userId) {
                JsonUtil.writeError(response, 404, "地址不存在");
                return;
            }

            String body = JsonUtil.readRequestBody(request);
            JsonObject json = JsonUtil.parseJson(body);

            if (json.has("receiverName")) address.setReceiverName(json.get("receiverName").getAsString());
            if (json.has("phone")) address.setPhone(json.get("phone").getAsString());
            if (json.has("province")) address.setProvince(json.get("province").getAsString());
            if (json.has("city")) address.setCity(json.get("city").getAsString());
            if (json.has("district")) address.setDistrict(json.get("district").getAsString());
            if (json.has("detailAddress")) address.setDetailAddress(json.get("detailAddress").getAsString());
            if (json.has("isDefault")) {
                boolean isDefault = json.get("isDefault").getAsBoolean();
                if (isDefault) {
                    addressDao.clearDefault(userId);
                }
                address.setDefault(isDefault);
            }

            addressDao.update(address);
            JsonUtil.writeSuccess(response, "更新成功", address);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Integer userId = (Integer) request.getAttribute("userId");
            if (userId == null) {
                JsonUtil.writeError(response, 401, "未登录");
                return;
            }

            String pathInfo = request.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                JsonUtil.writeError(response, 400, "地址ID不能为空");
                return;
            }

            int addressId = Integer.parseInt(pathInfo.substring(1));
            Address address = addressDao.findById(addressId);
            if (address == null || address.getUserId() != userId) {
                JsonUtil.writeError(response, 404, "地址不存在");
                return;
            }

            addressDao.delete(addressId);
            JsonUtil.writeSuccess(response, "删除成功", null);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.writeError(response, 500, "服务器错误");
        }
    }
}


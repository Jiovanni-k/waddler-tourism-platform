import axiosInstance from "./axiosInstance";

export const getLoyalty = () =>
  axiosInstance.get("loyalty/my");

export const redeemPoints = (points) =>
  axiosInstance.post("loyalty/my/redeem", { points });

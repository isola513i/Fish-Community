import { defineStore } from 'pinia';
import { ref } from 'vue';
import api from '../services/api';
import router from '../router';

export const useRoomStore = defineStore('room', () => {
  // === STATES ===
  const allRooms = ref([]); // สำหรับ User (Active เท่านั้น)
  const availableRooms = ref([]); // สำหรับ User (Active & ว่างตามเวลา)
  const adminRoomList = ref([]); // สำหรับ Admin (ทุกห้อง + สถานะจอง)
  const isLoading = ref(false);

  // === ACTIONS ===

  // (สำหรับหน้า BookRoom) ดึงห้อง Active ทั้งหมดมาเก็บไว้
  async function fetchAllRooms() {
    if (allRooms.value.length > 0) return; // ดึงแค่ครั้งเดียว

    isLoading.value = true;
    try {
      const response = await api.get('/rooms?size=100&sort=name');
      allRooms.value = response.data.content.filter((room) => room.isActive);
    } catch (error) {
      console.error('Failed to fetch all rooms:', error);
    } finally {
      isLoading.value = false;
    }
  }

  // (สำหรับหน้า BookRoom) ดึงห้องที่ว่างตามเวลา
  async function fetchAvailableRooms(startAt, endAt) {
    isLoading.value = true;
    availableRooms.value = [];
    try {
      const response = await api.get('/rooms/available', {
        params: { startAt, endAt },
      });
      availableRooms.value = response.data;
    } catch (error) {
      console.error('Failed to fetch available rooms:', error);
    } finally {
      isLoading.value = false;
    }
  }

  // (สำหรับหน้า ManageRooms Admin) ดึงห้องทั้งหมด + สถานะ
  async function fetchAdminRoomList() {
    isLoading.value = true;
    try {
      const response = await api.get('/rooms/admin-list');
      adminRoomList.value = response.data;
    } catch (error) {
      console.error('Failed to fetch admin room list:', error);
      adminRoomList.value = [];
    } finally {
      isLoading.value = false;
    }
  }

  // (สำหรับหน้า CreateRoom Admin)
  async function createRoom(roomData) {
    try {
      await api.post('/rooms', roomData);
      // (ไม่ต้อง push เข้า allRooms ก็ได้ เพราะ fetchAdminRoomList จะถูกเรียกเมื่อกลับไป)
      router.push({ name: 'AdminManageRooms' });
    } catch (error) {
      console.error('Failed to create room:', error);
      throw error;
    }
  }

  // (สำหรับหน้า EditRoom Admin)
  async function fetchRoom(id) {
    try {
      const response = await api.get(`/rooms/${id}`);
      return response.data;
    } catch (error) {
      console.error('Failed to fetch room:', error);
      router.push({ name: 'AdminManageRooms' });
    }
  }

  // ⭐️ (นี่คือฟังก์ชันที่ขาดไป) ⭐️
  // (สำหรับหน้า EditRoom Admin)
  async function updateRoom(id, roomData) {
    try {
      const response = await api.put(`/rooms/${id}`, roomData);

      const numericId = parseInt(id, 10);

      // อัปเดตใน List ของ Admin
      const index = adminRoomList.value.findIndex(
        (item) => item.room.id === numericId
      );
      if (index !== -1) {
        // อัปเดตข้อมูล room แต่เก็บข้อมูล nextBooking ไว้ (ถ้ามี)
        adminRoomList.value[index].room = response.data;
      }

      router.push({ name: 'AdminManageRooms' });
    } catch (error) {
      console.error('Failed to update room:', error);
      throw error;
    }
  }

  // === RETURN ===
  return {
    allRooms,
    availableRooms,
    adminRoomList,
    isLoading,
    fetchAllRooms,
    fetchAvailableRooms,
    fetchAdminRoomList,
    createRoom,
    fetchRoom,
    updateRoom, // ⬅️ ต้องมี `updateRoom` ตรงนี้
  };
});

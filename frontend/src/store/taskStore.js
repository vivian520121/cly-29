import { create } from 'zustand';

const useTaskStore = create((set) => ({
  tasks: [],
  kanbanData: [],
  loading: false,

  setTasks: (list) => {
    set({ tasks: list, loading: false });
  },

  setKanbanData: (data) => {
    set({ kanbanData: data, loading: false });
  },
}));

export default useTaskStore;

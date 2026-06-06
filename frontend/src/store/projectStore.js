import { create } from 'zustand';

const useProjectStore = create((set) => ({
  projects: [],
  currentProject: null,
  loading: false,

  setProjects: (list) => {
    set({ projects: list, loading: false });
  },

  setCurrentProject: (project) => {
    set({ currentProject: project });
  },
}));

export default useProjectStore;

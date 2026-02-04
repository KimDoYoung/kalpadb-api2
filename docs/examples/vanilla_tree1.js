<div class="flex h-screen" x-data="fileExplorer()">
    <!-- 트리 사이드바 -->
    <aside class="w-64 border-r bg-gray-50 overflow-auto">
        <div class="p-4">
            <h3 class="font-bold mb-2">Folders</h3>
            <div x-html="renderTree(treeData)"></div>
        </div>
    </aside>
    
    <!-- 메인 영역 -->
    <main class="flex-1 flex flex-col">
        <!-- 툴바 -->
        <header class="border-b bg-white p-4">
            <!-- Breadcrumb -->
            <nav class="flex items-center gap-2 mb-4">
                <template x-for="(item, idx) in breadcrumb" :key="idx">
                    <div class="flex items-center gap-2">
                        <button 
                            @click="navigateTo(item.id)"
                            class="hover:text-blue-600 text-sm"
                            x-text="item.name"
                        ></button>
                        <span x-show="idx < breadcrumb.length - 1" class="text-gray-400">/</span>
                    </div>
                </template>
            </nav>
            
            <!-- 액션 버튼 -->
            <div class="flex items-center justify-between">
                <div class="flex gap-2">
                    <button 
                        @click="uploadFile"
                        class="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                    >
                        ⬆️ Upload
                    </button>
                    <button 
                        @click="createFolder"
                        class="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
                    >
                        ➕ New Folder
                    </button>
                </div>
                
                <div class="flex gap-2">
                    <button 
                        @click="viewMode = 'grid'"
                        :class="viewMode === 'grid' ? 'bg-gray-700 text-white' : 'bg-gray-200'"
                        class="px-3 py-2 rounded"
                    >⊞</button>
                    <button 
                        @click="viewMode = 'list'"
                        :class="viewMode === 'list' ? 'bg-gray-700 text-white' : 'bg-gray-200'"
                        class="px-3 py-2 rounded"
                    >≡</button>
                </div>
            </div>
        </header>
        
        <!-- 파일 영역 -->
        <section class="flex-1 overflow-auto p-4 bg-gray-50">
            <!-- Grid View -->
            <div x-show="viewMode === 'grid'" class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
                <template x-for="item in currentFiles" :key="item.id">
                    <div 
                        @click="selectItem(item, $event)"
                        @dblclick="openItem(item)"
                        @contextmenu.prevent="showContextMenu($event, item)"
                        :class="isSelected(item.id) ? 'ring-2 ring-blue-500 bg-blue-50' : 'bg-white'"
                        class="border rounded-lg p-4 hover:shadow-md cursor-pointer transition group"
                    >
                        <div class="text-5xl text-center mb-2" x-text="getIcon(item)"></div>
                        <div class="text-sm text-center truncate font-medium" x-text="item.name"></div>
                        <template x-if="item.nodeType === 'F'">
                            <div class="text-xs text-gray-500 text-center mt-1" x-text="formatSize(item.fileSize)"></div>
                        </template>
                    </div>
                </template>
            </div>
            
            <!-- List View -->
            <div x-show="viewMode === 'list'" class="bg-white rounded-lg shadow">
                <table class="w-full">
                    <thead class="bg-gray-100 border-b">
                        <tr>
                            <th class="text-left p-3 font-semibold">Name</th>
                            <th class="text-left p-3 font-semibold">Size</th>
                            <th class="text-left p-3 font-semibold">Modified</th>
                            <th class="text-left p-3 font-semibold">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <template x-for="item in currentFiles" :key="item.id">
                            <tr 
                                @click="selectItem(item, $event)"
                                @dblclick="openItem(item)"
                                @contextmenu.prevent="showContextMenu($event, item)"
                                :class="isSelected(item.id) ? 'bg-blue-50' : ''"
                                class="border-b hover:bg-gray-50 cursor-pointer"
                            >
                                <td class="p-3">
                                    <div class="flex items-center gap-2">
                                        <span class="text-xl" x-text="getIcon(item)"></span>
                                        <span x-text="item.name"></span>
                                    </div>
                                </td>
                                <td class="p-3 text-gray-600" x-text="item.nodeType === 'D' ? '-' : formatSize(item.fileSize)"></td>
                                <td class="p-3 text-gray-600" x-text="formatDate(item.modifyDt)"></td>
                                <td class="p-3">
                                    <button @click.stop="downloadItem(item)" class="text-blue-600 hover:text-blue-800">⬇️</button>
                                    <button @click.stop="deleteItem(item)" class="text-red-600 hover:text-red-800 ml-2">🗑️</button>
                                </td>
                            </tr>
                        </template>
                    </tbody>
                </table>
            </div>
            
            <!-- Empty State -->
            <div x-show="currentFiles.length === 0" class="text-center py-16 text-gray-500">
                <div class="text-6xl mb-4">📂</div>
                <p>This folder is empty</p>
            </div>
        </section>
    </main>
    
    <!-- Context Menu -->
    <div 
        x-show="contextMenu.show"
        @click.away="contextMenu.show = false"
        :style="`top: ${contextMenu.y}px; left: ${contextMenu.x}px`"
        class="fixed bg-white shadow-lg rounded-lg border py-2 z-50"
    >
        <button @click="renameItem(contextMenu.item)" class="block w-full text-left px-4 py-2 hover:bg-gray-100">✏️ Rename</button>
        <button @click="moveItem(contextMenu.item)" class="block w-full text-left px-4 py-2 hover:bg-gray-100">➡️ Move</button>
        <button @click="downloadItem(contextMenu.item)" class="block w-full text-left px-4 py-2 hover:bg-gray-100">⬇️ Download</button>
        <hr class="my-1">
        <button @click="deleteItem(contextMenu.item)" class="block w-full text-left px-4 py-2 hover:bg-red-50 text-red-600">🗑️ Delete</button>
    </div>
</div>

<script>
function fileExplorer() {
    return {
        treeData: [],
        currentFiles: [],
        breadcrumb: [],
        currentFolderId: 'root',
        viewMode: 'grid',
        selectedIds: [],
        contextMenu: { show: false, x: 0, y: 0, item: null },
        
        async init() {
            await this.loadTree();
            await this.loadFolder('root');
        },
        
        async loadTree() {
            const response = await fetch('/api/nodes/tree');
            this.treeData = await response.json();
        },
        
        async loadFolder(folderId) {
            this.currentFolderId = folderId;
            
            // 현재 폴더의 파일 목록
            const filesResponse = await fetch(`/api/nodes/${folderId}/children`);
            this.currentFiles = await filesResponse.json();
            
            // Breadcrumb
            const breadcrumbResponse = await fetch(`/api/nodes/${folderId}/breadcrumb`);
            this.breadcrumb = await breadcrumbResponse.json();
            
            this.selectedIds = [];
        },
        
        renderTree(nodes, level = 0) {
            return nodes.map(node => {
                if (node.nodeType === 'D') {
                    return `
                        <div style="margin-left: ${level * 16}px" class="my-1">
                            <div 
                                class="flex items-center gap-2 px-2 py-1 rounded hover:bg-gray-200 cursor-pointer"
                                @click="loadFolder('${node.id}')"
                            >
                                <span>${node.expanded ? '📂' : '📁'}</span>
                                <span class="text-sm">${node.name}</span>
                            </div>
                            ${node.children ? this.renderTree(node.children, level + 1) : ''}
                        </div>
                    `;
                }
                return '';
            }).join('');
        },
        
        navigateTo(folderId) {
            this.loadFolder(folderId);
        },
        
        selectItem(item, event) {
            if (event.ctrlKey || event.metaKey) {
                // 다중 선택
                if (this.selectedIds.includes(item.id)) {
                    this.selectedIds = this.selectedIds.filter(id => id !== item.id);
                } else {
                    this.selectedIds.push(item.id);
                }
            } else {
                this.selectedIds = [item.id];
            }
        },
        
        isSelected(id) {
            return this.selectedIds.includes(id);
        },
        
        openItem(item) {
            if (item.nodeType === 'D') {
                this.loadFolder(item.id);
            } else {
                this.downloadItem(item);
            }
        },
        
        showContextMenu(event, item) {
            this.contextMenu = {
                show: true,
                x: event.clientX,
                y: event.clientY,
                item: item
            };
        },
        
        async uploadFile() {
            const input = document.createElement('input');
            input.type = 'file';
            input.multiple = true;
            input.onchange = async (e) => {
                const formData = new FormData();
                for (let file of e.target.files) {
                    formData.append('files', file);
                }
                formData.append('folderId', this.currentFolderId);
                
                await fetch('/api/files/upload', {
                    method: 'POST',
                    body: formData
                });
                
                this.loadFolder(this.currentFolderId);
            };
            input.click();
        },
        
        async createFolder() {
            const name = prompt('Folder name:');
            if (name) {
                await fetch('/api/nodes', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        nodeType: 'D',
                        parentId: this.currentFolderId,
                        name: name
                    })
                });
                this.loadFolder(this.currentFolderId);
            }
        },
        
        async renameItem(item) {
            const newName = prompt('New name:', item.name);
            if (newName && newName !== item.name) {
                await fetch(`/api/nodes/${item.id}`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ name: newName })
                });
                this.loadFolder(this.currentFolderId);
            }
            this.contextMenu.show = false;
        },
        
        async deleteItem(item) {
            if (confirm(`Delete ${item.name}?`)) {
                await fetch(`/api/nodes/${item.id}`, { method: 'DELETE' });
                this.loadFolder(this.currentFolderId);
            }
            this.contextMenu.show = false;
        },
        
        downloadItem(item) {
            if (item.nodeType === 'F') {
                window.open(`/api/files/${item.id}/download`, '_blank');
            }
            this.contextMenu.show = false;
        },
        
        getIcon(item) {
            if (item.nodeType === 'D') return '📁';
            
            const ext = item.name.split('.').pop()?.toLowerCase();
            const icons = {
                pdf: '📄', txt: '📄', doc: '📝', docx: '📝',
                jpg: '🖼️', jpeg: '🖼️', png: '🖼️', gif: '🖼️', svg: '🖼️',
                mp4: '🎬', avi: '🎬', mov: '🎬',
                mp3: '🎵', wav: '🎵',
                zip: '📦', rar: '📦', '7z': '📦',
                xls: '📊', xlsx: '📊', csv: '📊'
            };
            return icons[ext] || '📄';
        },
        
        formatSize(bytes) {
            if (!bytes) return '-';
            const units = ['B', 'KB', 'MB', 'GB'];
            let size = bytes;
            let unitIndex = 0;
            while (size >= 1024 && unitIndex < units.length - 1) {
                size /= 1024;
                unitIndex++;
            }
            return `${size.toFixed(1)} ${units[unitIndex]}`;
        },
        
        formatDate(dateStr) {
            if (!dateStr) return '-';
            return new Date(dateStr).toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        }
    }
}
</script>
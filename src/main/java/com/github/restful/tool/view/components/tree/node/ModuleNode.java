package com.github.restful.tool.view.components.tree.node;

import com.github.restful.tool.beans.ApiService;
import com.github.restful.tool.beans.ClassTree;
import com.github.restful.tool.beans.ModuleTree;
import com.github.restful.tool.view.components.tree.BaseNode;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.*;

/**
 * @author ZhangYuanSheng
 * @version 1.0
 */
public class ModuleNode extends DefaultNode<ModuleTree> {

    public ModuleNode(@NotNull ModuleTree source) {
        super(source);
    }

    @Override
    public @Nullable Icon getIcon(boolean selected) {
        return AllIcons.Nodes.Module;
    }

    public final static class Util {

        @NotNull
        public static List<BaseNode<?>> getChildren(@NotNull Map<PsiMethod, ServiceNode> serviceNodes, Map<PsiClass, List<ApiService>> collect, boolean showPackage) {
            if (collect == null || collect.isEmpty()) {
                return Collections.emptyList();
            }

            Map<String, PackageNode> packageNodeMap = new HashMap<>();
            List<BaseNode<?>> children = new ArrayList<>();
            List<BaseNode<?>> unKnownPackage = new ArrayList<>(0);
            List<BaseNode<?>> unKnownClass = new ArrayList<>(0);

            collect.forEach((psiClass, services) -> {
                if (psiClass == null) {
                    unKnownClass.addAll(getChildren(serviceNodes, services));
                    return;
                }
                ClassNode classNode = new ClassNode(new ClassTree(psiClass));
                services.forEach(apiService -> {
                    ServiceNode serviceNode = new ServiceNode(apiService);
                    if (apiService.getPsiElement() instanceof PsiMethod) {
                        serviceNodes.put((PsiMethod) apiService.getPsiElement(), serviceNode);
                    }
                    classNode.add(serviceNode);
                });

                if (!showPackage) {
                    // 不显示包名则直接添加到 module 节点
                    children.add(classNode);
                    return;
                }

                String qualifiedName = getPackageName(psiClass);
                if (qualifiedName == null) {
                    // 没有包名则直接添加到 module 节点
                    unKnownPackage.add(classNode);
                    return;
                }
                // packageNodeMap.computeIfAbsent(qualifiedName, PackageNode::new).add(classNode);
                customPending(packageNodeMap, qualifiedName).add(classNode);
            });

            // 抽取公共父包
            {
                List<PackageNode> nodes = new ArrayList<>();
                packageNodeMap.forEach((key, rootNode) -> {
                    while (true) {
                        List<PackageNode> list = findChildren(rootNode);
                        if (list.size() == 1) {
                            PackageNode newEle = list.get(0);
                            rootNode.remove(newEle);

                            newEle.setSource(rootNode.getSource() + "." + newEle.getSource());
                            rootNode = newEle;
                        } else {
                            break;
                        }
                    }

                    nodes.add(rootNode);
                });
                children.addAll(nodes);
            }

            if (!children.isEmpty()) {
                children.sort(Comparator.comparing(BaseNode::toString));
            }

            if (!unKnownPackage.isEmpty()) {
                unKnownPackage.sort(Comparator.comparing(BaseNode::toString));
                children.addAll(unKnownPackage);
            }

            if (!unKnownClass.isEmpty()) {
                children.addAll(unKnownClass);
            }
            return children;
        }

        @NotNull
        public static List<BaseNode<?>> getChildren(@NotNull Map<PsiMethod, ServiceNode> serviceNodes, List<ApiService> apiServices) {
            if (apiServices == null || apiServices.isEmpty()) {
                return Collections.emptyList();
            }

            List<BaseNode<?>> children = new ArrayList<>();
            apiServices.forEach(apiService -> {
                ServiceNode serviceNode = new ServiceNode(apiService);
                if (apiService.getPsiElement() instanceof PsiMethod) {
                    serviceNodes.put((PsiMethod) apiService.getPsiElement(), serviceNode);
                }
                children.add(serviceNode);
            });

            children.sort(Comparator.comparing(BaseNode::toString));
            return children;
        }

        private static PackageNode customPending(@NotNull Map<String, PackageNode> data, @NotNull String packageName) {
            String[] names = packageName.split("\\.");

            PackageNode node = data.computeIfAbsent(names[0], PackageNode::new);

            if (names.length == 1) {
                return node;
            }

            PackageNode curr = node;
            int fex = 1;
            while (fex < names.length) {
                String name = names[fex++];
                curr = findChild(curr, name);
            }

            return curr;
        }

        @NotNull
        private static PackageNode findChild(@NotNull PackageNode node, @NotNull String name) {
            Enumeration<TreeNode> children = node.children();
            while (children.hasMoreElements()) {
                TreeNode child = children.nextElement();
                if (!(child instanceof PackageNode)) {
                    continue;
                }
                PackageNode packageNode = (PackageNode) child;
                if (name.equals(packageNode.getSource())) {
                    return packageNode;
                }
            }
            PackageNode packageNode = new PackageNode(name);
            node.add(packageNode);
            return packageNode;
        }

        @NotNull
        private static List<PackageNode> findChildren(@NotNull PackageNode node) {
            List<PackageNode> children = new ArrayList<>();

            Enumeration<TreeNode> enumeration = node.children();
            while (enumeration.hasMoreElements()) {
                TreeNode ele = enumeration.nextElement();
                if (ele instanceof PackageNode) {
                    children.add((PackageNode) ele);
                }
            }

            return children;
        }

        @Nullable
        private static String getPackageName(@NotNull PsiClass psiClass) {
            String qualifiedName = psiClass.getQualifiedName();
            if (qualifiedName == null) {
                return null;
            }

            String fileName = psiClass.getName();
            if (fileName == null) {
                return null;
            }

            if (qualifiedName.endsWith(fileName)) {
                return qualifiedName.substring(0, qualifiedName.lastIndexOf('.'));
            }

            return null;
        }
    }
}

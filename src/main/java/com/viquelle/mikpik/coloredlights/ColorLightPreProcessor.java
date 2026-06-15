package com.viquelle.mikpik.coloredlights;

import com.viquelle.mikpik.MikpikMod;
import foundry.veil.api.client.render.shader.processor.ShaderPreProcessor;
import foundry.veil.impl.compat.sodium.SodiumShaderPreProcessor;
import io.github.ocelot.glslprocessor.api.GlslInjectionPoint;
import io.github.ocelot.glslprocessor.api.GlslParser;
import io.github.ocelot.glslprocessor.api.GlslSyntaxException;
import io.github.ocelot.glslprocessor.api.grammar.*;
import io.github.ocelot.glslprocessor.api.node.GlslNode;
import io.github.ocelot.glslprocessor.api.node.GlslNodeList;
import io.github.ocelot.glslprocessor.api.node.GlslTree;
import io.github.ocelot.glslprocessor.api.node.constant.GlslFloatConstantNode;
import io.github.ocelot.glslprocessor.api.node.function.GlslFunctionNode;
import io.github.ocelot.glslprocessor.api.node.function.GlslPrimitiveConstructorNode;
import io.github.ocelot.glslprocessor.api.node.variable.GlslNewFieldNode;
import io.github.ocelot.glslprocessor.api.node.variable.GlslVariableDeclarationNode;
import io.github.ocelot.glslprocessor.api.node.variable.GlslVariableNode;
import io.github.ocelot.glslprocessor.api.visitor.GlslNodeVisitor;
import io.github.ocelot.glslprocessor.lib.anarres.cpp.LexerException;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ColorLightPreProcessor implements ShaderPreProcessor {

    public static final ColorLightPreProcessor INSTANCE = new ColorLightPreProcessor();

    private static final ResourceLocation[] TARGET_SHADERS = {
            ResourceLocation.parse("sodium:shaders/blocks/block_layer_opaque.fsh"),
            ResourceLocation.parse("sodium:shaders/blocks/block_layer_opaque.vsh"),
    };

    private ColorLightPreProcessor() {}

    @Override
    public void modify(Context ctx, GlslTree tree) throws IOException, GlslSyntaxException, LexerException {
        ResourceLocation shaderName = ctx.name();
        MikpikMod.LOGGER.info("{}", shaderName);
        boolean isTarget = false;
        for (ResourceLocation target : TARGET_SHADERS) {
            if (shaderName.equals(target)) {
                isTarget = true;
                break;
            }
        }

        if (!isTarget) return;

        MikpikMod.LOGGER.debug("Processing shader: {} ({})", shaderName, ctx.type());

        if (ctx.isVertex()) {
            modifyVertexShader(ctx, tree);
        } else if (ctx.isFragment()) {
            modifyFragmentShader(tree);
        }
    }

    private void modifyVertexShader(Context ctx, GlslTree tree) throws GlslSyntaxException, IOException, LexerException {
//        MikpikMod.LOGGER.debug("1 vertex RESULT BOBINA to source string: {}", tree.toSourceString());
        addNewUniforms(tree);
//        MikpikMod.LOGGER.debug("2 vertex RESULT BOBINA to source string: {}", tree.toSourceString());
        addOutputVariables(tree);
//        MikpikMod.LOGGER.debug("3 vertex RESULT BOBINA to source string: {}", tree.toSourceString());
        addLightingFunction(ctx, tree);
//        MikpikMod.LOGGER.debug("4 vertex RESULT BOBINA to source string: {}", tree.toSourceString());
        modifyVertexMain(tree);
        MikpikMod.LOGGER.debug("5 vertex RESULT BOBINA to source string: {}", tree.toSourceString());
    }

    private void modifyFragmentShader(GlslTree tree) throws GlslSyntaxException {
        addFragmentInputVariables(tree);
        modifyFragmentMain(tree);
        MikpikMod.LOGGER.debug("fragment RESULT BOBINA to source string: {}", tree.toSourceString());
    }

    private void addNewUniforms(GlslTree tree) throws GlslSyntaxException {
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN,
                GlslParser.parseExpression("uniform int u_light_count;"));
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN,
                GlslParser.parseExpression("uniform vec4 u_LightData[256];"));
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN,
                GlslParser.parseExpression("uniform vec4 u_LightColor[256];"));
    }

    private void addOutputVariables(GlslTree tree) throws GlslSyntaxException {
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN,
                GlslParser.parseExpression("out vec4 blockLightColor;"));
    }

    private void addLightingFunction(Context ctx, GlslTree tree) throws GlslSyntaxException, IOException, LexerException {
        String source = """
vec4 getBlockLightColor(vec3 position, int count, vec4 LightData[256], vec4 LightColor[256])
{
    vec3 colorSum = vec3(0.0);
    float totalWeight = 0.0;
    float maxIntensity = 0.0;
    for(int i = 0; i < count; i++)
    {
        vec4 lightPosRad = LightData[i];
        vec4 lightColorIntensity = LightColor[i];

        vec3 delta = lightPosRad.xyz - position;

        float radius = lightPosRad.w;
        float dist = abs(delta.x) + abs(delta.y) + abs(delta.z);

        if(dist > radius)
            continue;

        float falloff = 1.0 - dist / radius;
        float weight = falloff * lightColorIntensity.a;
        
        colorSum += lightColorIntensity.rgb * weight;
        totalWeight += weight;
        maxIntensity = max(maxIntensity, weight);
    }

    if (totalWeight <= 0.001) 
        return vec4(0.0);
 
    vec3 avgColor = colorSum/totalWeight;
    float intensity = 0.0;
    
    if (maxIntensity > 1.0) {
        intensity = clamp(totalWeight, 0.0, maxIntensity);
    } else {
        intensity = clamp(totalWeight, 0.0, 1.0);
    }
    
    return vec4(avgColor, intensity);
}
                """;
        GlslTree includeTree = GlslParser.parse(source);
        ctx.include(tree, "mikpik:colored_light", includeTree, IncludeOverloadStrategy.SOURCE);
    }


    private void modifyVertexMain(GlslTree tree) throws GlslSyntaxException {
        var mainOpt = tree.mainFunction();
        if (mainOpt.isEmpty()) return;

        GlslFunctionNode main = mainOpt.get();
        GlslNodeList body = main.getBody();

        body.add(GlslParser.parseExpression("float block = float((a_LightAndData.r >> 4u) & 15u) / 15.0;"));
        body.add(GlslParser.parseExpression("float factor = block > 0.00 ? 1.0 : 0.0;"));
        String func = """
                if (factor != 0.0) {
                    blockLightColor = getBlockLightColor(position, u_light_count, u_LightData, u_LightColor);
                } else {
                    blockLightColor = vec4(0.0);
                }
                """;
        body.add(GlslParser.parseExpression(func));
    }

    private void addFragmentInputVariables(GlslTree tree) throws GlslSyntaxException {
        tree.getBody().add(GlslInjectionPoint.BEFORE_MAIN,
                GlslParser.parseExpression("in vec4 blockLightColor;"));
    }

    private void modifyFragmentMain(GlslTree tree) throws GlslSyntaxException {
        var mainOpt = tree.mainFunction();
        if (mainOpt.isEmpty()) return;

        GlslFunctionNode main = mainOpt.get();
        GlslNodeList body = main.getBody();

        String colorModCode = """
if(blockLightColor.a > 0.0) {
    float lightAmount = clamp(blockLightColor.a, 0.0, 1.0);
    
    float lightLum = dot(blockLightColor.rgb, vec3(0.2126, 0.7152, 0.0722));
    vec3 normalizedLight = lightLum > 0.001 ? blockLightColor.rgb / lightLum * max(1.0, blockLightColor.a) : blockLightColor.rgb;
    
    vec3 lightTint = mix(vec3(1.0), normalizedLight, lightAmount);
    color.rgb = color.rgb * lightTint;
}
    """;

        for (int i = 0; i < body.size(); i++) {
            GlslNode node = body.get(i);

            String src = node.toSourceString();

            if (src.contains("vec4 color")) {
                body.add(i + 1, GlslParser.parseExpression(colorModCode));
                return;
            }
        }

        MikpikMod.LOGGER.warn("Could not find vec4 color declaration in fragment shader");
    }
}